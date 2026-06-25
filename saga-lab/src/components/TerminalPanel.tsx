import { useEffect, useRef } from "react";
import type { LabEvent } from "../types";

type Props = {
  events: LabEvent[];
};

export function TerminalPanel({ events }: Props) {
  const terminalRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    terminalRef.current?.scrollTo({ top: terminalRef.current.scrollHeight });
  }, [events]);

  return (
    <section className="terminal-panel" aria-label="Event terminal">
      <div className="terminal-header">
        <h2>Terminal</h2>
        <span>{events.length} events</span>
      </div>
      <div className="terminal-body" ref={terminalRef}>
        {events.length === 0 && <p className="terminal-empty">Waiting for lab events...</p>}
        {events.map((event) => (
          <div className={`terminal-line ${event.level}`} key={event.id}>
            <span className="terminal-time">[{new Date(event.timestamp).toLocaleTimeString()}]</span>
            <span className={`terminal-source ${event.source}`}>[{event.source}]</span>
            <span>{event.message}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
