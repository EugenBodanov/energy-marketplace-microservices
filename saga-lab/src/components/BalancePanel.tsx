import type { LabStatus } from "../types";

type Props = {
  status: LabStatus;
};

function money(value?: string, currency = "EUR") {
  return value ? `${value} ${currency}` : "pending";
}

export function BalancePanel({ status }: Props) {
  const buyerCurrency = status.buyer?.currency ?? "EUR";
  const sellerCurrency = status.seller?.currency ?? "EUR";
  const hasTrade = Boolean(status.tradeId);

  return (
    <section className="panel balance-panel" aria-label="Buyer and seller balances">
      <div className="panel-title">
        <h2>Balances</h2>
        <span>{hasTrade ? `trade #${status.tradeId}` : "waiting for trade"}</span>
      </div>

      <div className="balance-row">
        <span>Buyer</span>
        <strong className="buyer-balance">{money(status.buyer?.balance, buyerCurrency)}</strong>
        <small>reserved {money(status.buyer?.reserved, buyerCurrency)}</small>
      </div>

      <div className="balance-row">
        <span>Seller</span>
        <strong className="seller-balance">{money(status.seller?.balance, sellerCurrency)}</strong>
        <small>reserved {money(status.seller?.reserved, sellerCurrency)}</small>
      </div>

      <div className="balance-note">
        Values are read from the billing database through the billing API.
      </div>
    </section>
  );
}
