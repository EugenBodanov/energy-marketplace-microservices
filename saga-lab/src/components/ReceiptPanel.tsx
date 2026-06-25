import type { LabStatus } from "../types";

type Props = {
  status: LabStatus;
};

export function ReceiptPanel({ status }: Props) {
  const receipt = status.receipt;

  if (!receipt?.receiptId) {
    return (
      <section className="panel receipt-panel" aria-label="Generated receipt">
        <div className="panel-title">
          <h2>Receipt</h2>
          <span>pending</span>
        </div>
        <div className="receipt-empty">
          <strong>Waiting for generation</strong>
          <span>After settlement, Billing receives the receipt command and publishes the receipt event.</span>
        </div>
      </section>
    );
  }

  return (
    <section className="panel receipt-panel" aria-label="Generated receipt">
      <div className="panel-title">
        <h2>Receipt</h2>
        <span>#{receipt.receiptId}</span>
      </div>

      <div className="receipt-card">
        <div>
          <span>Trade</span>
          <strong>{receipt.tradeId}</strong>
        </div>
        <div>
          <span>Amount</span>
          <strong>
            {receipt.amount} {receipt.currency}
          </strong>
        </div>
        <div>
          <span>Buyer</span>
          <strong>{receipt.buyerId}</strong>
        </div>
        <div>
          <span>Seller</span>
          <strong>{receipt.sellerId}</strong>
        </div>
        <div>
          <span>Listing</span>
          <strong>{receipt.listingId}</strong>
        </div>
        <div>
          <span>Generated</span>
          <strong>{new Date(receipt.generatedAt).toLocaleString()}</strong>
        </div>
      </div>
    </section>
  );
}
