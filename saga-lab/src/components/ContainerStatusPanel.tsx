import type { ContainerState } from "../types";

type Props = {
  containers: ContainerState[];
};

const expectedServices = [
  "rabbitmq",
  "user-db",
  "listing-db",
  "trade-db",
  "billing-db",
  "user-service",
  "listing-service",
  "trade-service",
  "billing-service",
  "api-gateway",
  "adminer",
];

function statusLabel(container: ContainerState) {
  if (container.health && container.health !== "none") {
    return container.health;
  }
  return container.state || "not created";
}

function statusClass(label: string) {
  return label.replace(/\s+/g, "-");
}

export function ContainerStatusPanel({ containers }: Props) {
  const containersByService = new Map(containers.map((container) => [container.service, container]));
  const rows = expectedServices.map(
    (service) =>
      containersByService.get(service) ?? {
        service,
        container: "",
        state: "not created",
        health: "none",
        status: "",
      }
  );

  return (
    <section className="panel container-panel" aria-label="Runtime status">
      <div className="panel-title">
        <h2>Runtime</h2>
        <span>{containers.length} active</span>
      </div>
      <div className="container-list">
        {rows.map((container) => (
          <div className="container-row" key={container.service}>
            <span>{container.service}</span>
            <strong className={`health ${statusClass(statusLabel(container))}`}>
              {statusLabel(container)}
            </strong>
          </div>
        ))}
      </div>
    </section>
  );
}
