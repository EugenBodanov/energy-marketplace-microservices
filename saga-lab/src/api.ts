import type { LabStatus, ScenarioId } from "./types";

const apiBase = import.meta.env.VITE_SAGA_LAB_API ?? "http://127.0.0.1:8090";

async function post<T>(path: string, body?: unknown): Promise<T> {
  const response = await fetch(`${apiBase}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed with HTTP ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export async function getStatus(): Promise<LabStatus> {
  const response = await fetch(`${apiBase}/api/status`);
  if (!response.ok) {
    throw new Error(`Status request failed with HTTP ${response.status}`);
  }
  return response.json();
}

export function connectEvents(onMessage: (event: MessageEvent) => void): EventSource {
  const source = new EventSource(`${apiBase}/api/events`);
  source.onmessage = onMessage;
  return source;
}

export function startDocker() {
  return post<{ ok: boolean }>("/api/docker/start");
}

export function resetDocker() {
  return post<{ ok: boolean }>("/api/docker/reset");
}

export function resetRun() {
  return post<{ ok: boolean }>("/api/lab/reset");
}

export function startScenario(scenarioId: ScenarioId) {
  return post<{ ok: boolean }>("/api/scenarios/start", { scenarioId });
}
