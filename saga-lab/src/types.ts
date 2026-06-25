export type ScenarioId =
  | "happy-path"
  | "reservation-failure"
  | "payment-failure";

export type Scenario = {
  id: ScenarioId;
  title: string;
  description: string;
  buyerBalance: string;
  tradeAmount: string;
  phases: string[];
  failureMessages?: Record<string, string>;
  /** Phases in this scenario where a compensation path can be entered. */
  compensationEntryPoints?: string[];
  /** Phases that execute via synchronous REST rather than async messaging. */
  syncRestPhases?: string[];
};

export type LabEvent = {
  id: number;
  timestamp: string;
  level: "debug" | "info" | "success" | "warn" | "error";
  source:
    | "api"
    | "billing"
    | "billing-service"
    | "docker"
    | "health"
    | "listing-service"
    | "scenario"
    | "trade"
    | "trade-service"
    | "ui";
  message: string;
  details?: Record<string, unknown>;
};

export type ContainerState = {
  service: string;
  container: string;
  state: string;
  health: string;
  status: string;
};

export type AccountSnapshot = {
  userId?: number;
  balance?: string;
  reserved?: string;
  currency?: string;
};

export type ReceiptSnapshot = {
  tradeId: number;
  receiptId: number;
  buyerId: number;
  sellerId: number;
  listingId: number;
  amount: string;
  currency: string;
  generatedAt: string;
};

export type ListingSnapshot = {
  id: number;
  sellerId?: number;
  title?: string;
  priceAmount?: number;
  priceCurrency?: string;
  capacityValue?: number;
  capacityUnit?: string;
  status: string;
  reservationReference?: number | null;
  updatedAt?: string;
  error?: string;
};

export type LabStatus = {
  running: boolean;
  completedPhases: string[];
  failedPhases: string[];
  currentOperation?: string;
  activeScenario?: ScenarioId;
  activePhase?: string;
  phaseMode?: "idle" | "running";
  tradeStatus?: string;
  phaseStatusDetails: Record<string, string>;
  snapshots: SagaSnapshot[];
  tradeId?: number;
  listingId?: number;
  buyer?: AccountSnapshot;
  seller?: AccountSnapshot;
  receipt?: ReceiptSnapshot;
  listing?: ListingSnapshot;
  containers: ContainerState[];
  stackReady: boolean;
};

export type SagaSnapshot = {
  phase: string;
  tradeStatus?: string;
  capturedAt: string;
  buyer?: AccountSnapshot;
  seller?: AccountSnapshot;
  listing?: ListingSnapshot;
  receipt?: ReceiptSnapshot;
};
