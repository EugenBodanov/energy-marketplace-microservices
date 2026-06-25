import type { Scenario } from "../types";

export const scenarios: Scenario[] = [
  {
    id: "happy-path",
    title: "Happy Path",
    description: "Buyer has enough funds and the trade completes with a receipt.",
    buyerBalance: "1000.00 EUR",
    tradeAmount: "100.00 EUR",
    phases: [
      "Created",
      "Validate Users",
      "Reserve Listing",
      "Authorize Payment",
      "Close Listing",
      "Settle Payment",
      "Generate Receipt",
      "Completed",
    ],
    compensationEntryPoints: ["Authorize Payment", "Close Listing", "Settle Payment"],
    syncRestPhases: ["Validate Users"],
  },
  {
    id: "reservation-failure",
    title: "Reservation Failure",
    description: "The listing cannot be reserved, so the trade fails before payment.",
    buyerBalance: "1000.00 EUR",
    tradeAmount: "10.00 EUR",
    phases: ["Created", "Validate Users", "Reserve Listing", "Failed"],
    failureMessages: {
      "Reserve Listing": "Listing was missing or unavailable, so reservation could not be completed.",
      Failed: "Trade failed before payment authorization.",
    },
    syncRestPhases: ["Validate Users"],
  },
  {
    id: "payment-failure",
    title: "Payment Failure",
    description: "Buyer has insufficient funds, so authorization fails and the listing is compensated.",
    buyerBalance: "0.00 EUR",
    tradeAmount: "100.00 EUR",
    phases: [
      "Created",
      "Validate Users",
      "Reserve Listing",
      "Authorize Payment",
      "Compensate Listing",
      "Failed",
    ],
    failureMessages: {
      "Authorize Payment": "Insufficient funds. Buyer balance is lower than the requested trade amount.",
      Failed: "Trade failed after listing compensation completed.",
    },
    compensationEntryPoints: ["Authorize Payment"],
    syncRestPhases: ["Validate Users"],
  },
];

export const allPhases = [
  "Created",
  "Validate Users",
  "Reserve Listing",
  "Authorize Payment",
  "Close Listing",
  "Settle Payment",
  "Compensate Listing",
  "Generate Receipt",
  "Completed",
  "Failed",
];
