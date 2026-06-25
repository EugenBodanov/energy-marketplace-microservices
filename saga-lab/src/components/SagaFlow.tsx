import type { SagaSnapshot, Scenario } from "../types";

type Props = {
  scenario: Scenario;
  activePhase?: string;
  completedPhases: string[];
  failedPhases: string[];
  phaseStatusDetails: Record<string, string>;
  phaseMode?: "idle" | "running";
  canInspect: boolean;
  selectedPhase: string | null;
  snapshots: SagaSnapshot[];
  onSelectPhase: (phase: string) => void;
};

export function SagaFlow({
  scenario,
  activePhase,
  completedPhases,
  failedPhases,
  phaseStatusDetails,
  phaseMode = "idle",
  canInspect,
  selectedPhase,
  snapshots,
  onSelectPhase,
}: Props) {
  const completed = new Set(completedPhases);
  const failed = new Set(failedPhases);
  const snapshotPhases = new Set(snapshots.map((snapshot) => snapshot.phase));

  return (
    <ol className="flow-stepper" aria-label="Simplified saga steps">
      {scenario.phases.map((phase, scenarioIndex) => {
        const isDone = completed.has(phase);
        const isFailed = failed.has(phase);
        const isActive = activePhase === phase && !isDone && !isFailed;
        const isCompensationEntry = scenario.compensationEntryPoints?.includes(phase) ?? false;
        const isSyncRest = scenario.syncRestPhases?.includes(phase) ?? false;
        const isRunning = isActive && phaseMode === "running";
        const isCurrent = isActive && !isRunning;
        const isPending = !isDone && !isFailed && !isActive;
        const isInspectable = canInspect && snapshotPhases.has(phase);
        const isSelected = selectedPhase === phase;
        const failureMessage = scenario.failureMessages?.[phase];
        const observedStatus = phaseStatusDetails[phase];
        const badge = isRunning
          ? "Running"
          : isCurrent
            ? "Ready"
            : isFailed
              ? "Failed"
              : isDone
                ? "Done"
                : "Not Started";
        const className = [
          "flow-step",
          isDone ? "done" : "",
          isFailed ? "failed" : "",
          isActive ? "active" : "",
          isPending ? "pending" : "",
          isCurrent ? "current" : "",
          isRunning ? "running" : "",
          isInspectable ? "inspectable" : "",
          isSelected ? "selected" : "",
          isCompensationEntry ? "compensation-entry" : "",
          phase.includes("Failed") ? "failure" : "",
          phase === "Completed" ? "success" : "",
          phase === "Compensate Listing" ? "compensation" : "",
        ]
          .filter(Boolean)
          .join(" ");

        return (
          <li className={className} key={`${phase}-${scenarioIndex}`}>
            <div className="step-marker" aria-hidden="true">
              {isRunning ? <span className="step-spinner" /> : <span>{scenarioIndex + 1}</span>}
            </div>
            <button
              className="step-card"
              disabled={!isInspectable}
              onClick={() => onSelectPhase(phase)}
              type="button"
            >
              <div>
                <strong>{phase}</strong>
                {observedStatus && <small className="observed-status">{observedStatus}</small>}
                {isInspectable && <small>snapshot available</small>}
                {isCompensationEntry && <small>rollback entry point</small>}
                {isSyncRest && <small className="sync-rest-badge">sync REST</small>}
                {phase === "Compensate Listing" && <small>release reserved listing</small>}
              </div>
              <span className="step-state">
                <span title={isFailed ? failureMessage : undefined}>{badge}</span>
              </span>
            </button>
          </li>
        );
      })}
    </ol>
  );
}
