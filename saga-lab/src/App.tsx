import { useEffect, useMemo, useState } from "react";
import {
  connectEvents,
  getStatus,
  resetDocker,
  resetRun,
  startDocker,
  startScenario,
} from "./api";
import { BalancePanel } from "./components/BalancePanel";
import { ContainerStatusPanel } from "./components/ContainerStatusPanel";
import { Controls } from "./components/Controls";
import { ListingPanel } from "./components/ListingPanel";
import { ReceiptPanel } from "./components/ReceiptPanel";
import { SagaFlow } from "./components/SagaFlow";
import { ScenarioSelector } from "./components/ScenarioSelector";
import { TerminalPanel } from "./components/TerminalPanel";
import { scenarios } from "./data/scenarios";
import type { LabEvent, LabStatus, ScenarioId } from "./types";

const initialStatus: LabStatus = {
  running: false,
  completedPhases: [],
  failedPhases: [],
  phaseStatusDetails: {},
  snapshots: [],
  containers: [],
  stackReady: false,
};

export default function App() {
  const [selectedScenario, setSelectedScenario] = useState<ScenarioId>("happy-path");
  const [status, setStatus] = useState<LabStatus>(initialStatus);
  const [events, setEvents] = useState<LabEvent[]>([]);
  const [busyAction, setBusyAction] = useState<string | null>(null);
  const [selectedSnapshotPhase, setSelectedSnapshotPhase] = useState<string | null>(null);

  const scenario = useMemo(
    () => scenarios.find((item) => item.id === selectedScenario) ?? scenarios[0],
    [selectedScenario]
  );

  const readyContainers = status.containers.filter(
    (container) =>
      container.state === "running" &&
      (container.health === "healthy" || container.health === "none")
  ).length;
  const canResetRun =
    !status.running &&
    Boolean(
      status.activeScenario ||
        status.activePhase ||
        status.completedPhases.length ||
        status.failedPhases.length ||
        status.tradeId ||
        (status.receipt && Object.keys(status.receipt).length)
    );
  const selectedSnapshot = selectedSnapshotPhase
    ? status.snapshots.find((snapshot) => snapshot.phase === selectedSnapshotPhase)
    : undefined;
  const displayStatus: LabStatus = selectedSnapshot
    ? {
        ...status,
        buyer: selectedSnapshot.buyer,
        seller: selectedSnapshot.seller,
        listing: selectedSnapshot.listing,
        receipt: selectedSnapshot.receipt,
        tradeStatus: selectedSnapshot.tradeStatus,
      }
    : status;

  useEffect(() => {
    const refresh = async () => {
      try {
        setStatus(await getStatus());
      } catch {
        setStatus((current) => ({ ...current, containers: [] }));
      }
    };

    refresh();
    const interval = window.setInterval(refresh, 2500);
    return () => window.clearInterval(interval);
  }, []);

  useEffect(() => {
    const source = connectEvents((message) => {
      const event = JSON.parse(message.data) as LabEvent;
      setEvents((current) => [...current.slice(-499), event]);
      getStatus().then(setStatus).catch(() => undefined);
    });

    source.onerror = () => {
      setEvents((current) => [
        ...current.slice(-499),
        {
          id: Date.now(),
          timestamp: new Date().toISOString(),
          level: "warn",
          source: "ui",
          message: "Event stream disconnected. The UI will keep polling status.",
        },
      ]);
    };

    return () => source.close();
  }, []);

  async function runAction(name: string, action: () => Promise<unknown>) {
    setBusyAction(name);
    try {
      await action();
      setStatus(await getStatus());
    } catch (error) {
      setEvents((current) => [
        ...current.slice(-499),
        {
          id: Date.now(),
          timestamp: new Date().toISOString(),
          level: "error",
          source: "ui",
          message: error instanceof Error ? error.message : "UI action failed",
        },
      ]);
    } finally {
      setBusyAction(null);
    }
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Energy Marketplace</p>
          <h1>Saga Lab</h1>
        </div>
        <div className="status-cluster" aria-label="Lab status">
          <div className={`status-chip ${status.stackReady ? "ready" : "idle"}`}>
            <span className="status-dot" />
            {status.stackReady ? "Stack ready" : "Stack not ready"}
          </div>
          <div className="status-meter">
            <strong>{readyContainers}</strong>
            <span>containers ready</span>
          </div>
        </div>
      </header>

      <section className="control-dock" aria-label="Run controls">
        <Controls
          busyAction={busyAction}
          isRunning={status.running}
          isStackReady={status.stackReady}
          canResetRun={canResetRun}
          operation={status.currentOperation}
          onStartStack={() => runAction("start-stack", startDocker)}
          onResetStack={() => runAction("reset-stack", resetDocker)}
          onResetRun={() =>
            runAction("reset-run", async () => {
              await resetRun();
              setEvents([]);
              setSelectedSnapshotPhase(null);
            })
          }
          onStartScenario={() =>
            runAction("start-scenario", () => {
              setSelectedSnapshotPhase(null);
              return startScenario(selectedScenario);
            })
          }
        />
      </section>

      <section className="workspace-grid">
        <aside className="left-rail">
          <ScenarioSelector
            scenarios={scenarios}
            selectedScenario={selectedScenario}
            onSelect={setSelectedScenario}
          />
          <BalancePanel status={displayStatus} />
          <ReceiptPanel status={displayStatus} />
        </aside>

        <section className="main-stage" aria-label="Saga flow">
          <div className="stage-header">
            <div>
              <h2>{scenario.title}</h2>
              <p>{scenario.description}</p>
            </div>
            <div className="stage-meta">
              <span>{scenario.phases.length} steps</span>
              <div className={`run-pill ${status.running ? "running" : status.stackReady ? "ready" : "idle"}`}>
              {status.currentOperation ??
                (selectedSnapshot
                  ? "Inspecting snapshot"
                  : status.activeScenario
                    ? `Running: ${status.activeScenario}`
                    : status.stackReady
                      ? "Stack ready"
                      : "Stack idle")}
              </div>
              {selectedSnapshot && (
                <button className="snapshot-live-button" onClick={() => setSelectedSnapshotPhase(null)} type="button">
                  Live / Final State
                </button>
              )}
            </div>
          </div>
          <SagaFlow
            activePhase={status.activePhase}
            completedPhases={status.completedPhases}
            failedPhases={status.failedPhases}
            phaseStatusDetails={status.phaseStatusDetails}
            phaseMode={status.phaseMode}
            canInspect={!status.running && status.snapshots.length > 0}
            selectedPhase={selectedSnapshotPhase}
            scenario={scenario}
            snapshots={status.snapshots}
            onSelectPhase={setSelectedSnapshotPhase}
          />
        </section>

        <aside className="right-rail">
          <ContainerStatusPanel containers={status.containers} />
          <ListingPanel status={displayStatus} />
        </aside>
      </section>

      <TerminalPanel events={events} />
    </main>
  );
}
