type Props = {
  busyAction: string | null;
  isRunning: boolean;
  isStackReady: boolean;
  canResetRun: boolean;
  operation?: string;
  onStartStack: () => void;
  onResetStack: () => void;
  onResetRun: () => void;
  onStartScenario: () => void;
};

export function Controls({
  busyAction,
  isRunning,
  isStackReady,
  canResetRun,
  operation,
  onStartStack,
  onResetStack,
  onResetRun,
  onStartScenario,
}: Props) {
  const isBusy = busyAction !== null || Boolean(operation);
  const controlBusy = busyAction !== null;

  return (
    <div className="controls" aria-label="Saga controls">
      <div className="control-group infrastructure">
        <button className="secondary-action" disabled={isBusy} onClick={onStartStack} type="button">
          {busyAction === "start-stack" || operation === "start stack" ? (
            <span className="button-loader" />
          ) : null}
          Start Containers
        </button>
        <button className="danger-action" disabled={isBusy} onClick={onResetStack} type="button">
          {busyAction === "reset-stack" || operation === "reset stack" ? (
            <span className="button-loader" />
          ) : null}
          Reset Volumes
        </button>
        <button
          className="secondary-action"
          disabled={controlBusy || isRunning || !canResetRun}
          onClick={onResetRun}
          type="button"
        >
          Reset Run
        </button>
      </div>

      <div className="control-group scenario-controls">
        <button
          className="primary-action"
          disabled={isBusy || isRunning || !isStackReady}
          onClick={onStartScenario}
          type="button"
        >
          {busyAction === "start-scenario" || operation?.startsWith("scenario") ? (
            <span className="button-loader" />
          ) : null}
          Start Saga
        </button>
      </div>
    </div>
  );
}
