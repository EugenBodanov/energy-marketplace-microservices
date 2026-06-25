import type { Scenario, ScenarioId } from "../types";

type Props = {
  scenarios: Scenario[];
  selectedScenario: ScenarioId;
  onSelect: (scenarioId: ScenarioId) => void;
};

export function ScenarioSelector({ scenarios, selectedScenario, onSelect }: Props) {
  return (
    <aside className="scenario-panel" aria-label="Scenarios">
      <h2>Scenarios</h2>
      <div className="scenario-list">
        {scenarios.map((scenario) => (
          <button
            className={scenario.id === selectedScenario ? "scenario-card active" : "scenario-card"}
            key={scenario.id}
            onClick={() => onSelect(scenario.id)}
            type="button"
          >
            <span>{scenario.title}</span>
            <small>{scenario.tradeAmount} trade · buyer {scenario.buyerBalance}</small>
          </button>
        ))}
      </div>
    </aside>
  );
}
