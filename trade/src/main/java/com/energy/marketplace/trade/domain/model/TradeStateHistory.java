package com.energy.marketplace.trade.domain.model;

import java.time.Instant;

@lombok.Getter
public class TradeStateHistory {

    private Long id;
    private Long tradeId;
    private TradeStatus fromStatus;
    private TradeStatus toStatus;
    private String reason;
    private Instant changedAt;

    public TradeStateHistory(
            Long tradeId,
            TradeStatus fromStatus,
            TradeStatus toStatus,
            String reason
    ) {
        this.tradeId = tradeId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.changedAt = Instant.now();
    }
}
