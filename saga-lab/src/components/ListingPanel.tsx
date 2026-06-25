import type { LabStatus } from "../types";

type Props = {
  status: LabStatus;
};

function readableError(error: string): string {
  const httpMatch = error.match(/HTTP (\d+)/);
  if (httpMatch) return `Request failed with HTTP ${httpMatch[1]}.`;
  return error.length > 140 ? error.slice(0, 140) + "…" : error;
}

export function ListingPanel({ status }: Props) {
  const listing = status.listing;
  const isNotFound = listing?.status === "NOT_FOUND" || listing?.error?.includes("NOT_FOUND");

  if (!listing?.status || isNotFound) {
    return (
      <section className="panel listing-panel" aria-label="Listing database state">
        <div className="panel-title">
          <h2>Listing State</h2>
          <span>{status.listingId ? `#${status.listingId}` : "waiting for trade"}</span>
        </div>
        <div className="listing-empty">
          <strong>No listing</strong>
          <span>
            {isNotFound
              ? "Listing not found — the reservation failure scenario uses a non-existent listing to trigger the failure."
              : "The listing snapshot appears after the trade is created."}
          </span>
        </div>
      </section>
    );
  }

  return (
    <section className="panel listing-panel" aria-label="Listing database state">
      <div className="panel-title">
        <h2>Listing State</h2>
        <span>{status.listingId ? `#${status.listingId}` : "waiting for trade"}</span>
      </div>

      {listing.error ? (
        <div className="listing-empty error">
          <strong>{listing.status}</strong>
          <span>{readableError(listing.error)}</span>
        </div>
      ) : (
        <div className="listing-grid">
          <div>
            <span>Status</span>
            <strong className={`listing-status ${listing.status.toLowerCase()}`}>{listing.status}</strong>
          </div>
          <div>
            <span>Reservation</span>
            <strong>{listing.reservationReference ?? "none"}</strong>
          </div>
          <div>
            <span>Price</span>
            <strong>
              {listing.priceAmount} {listing.priceCurrency}
            </strong>
          </div>
          <div>
            <span>Capacity</span>
            <strong>
              {listing.capacityValue} {listing.capacityUnit}
            </strong>
          </div>
          <div className="listing-wide">
            <span>Title</span>
            <strong>{listing.title}</strong>
          </div>
          <div className="listing-wide">
            <span>Updated</span>
            <strong>{listing.updatedAt ? new Date(listing.updatedAt).toLocaleString() : "pending"}</strong>
          </div>
        </div>
      )}
    </section>
  );
}
