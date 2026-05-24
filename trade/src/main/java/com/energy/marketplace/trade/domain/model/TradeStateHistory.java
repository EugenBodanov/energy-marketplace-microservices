package com.energy.marketplace.trade.domain.model;

import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReason;

import java.time.Instant;

@lombok.Getter
public class TradeStateHistory {

    private Long id;
    private Long tradeId;
    private TradeStatus fromStatus;
    private TradeStatus toStatus;
    private TradeStateTransitionReason reason;
    private Instant changedAt;

    public TradeStateHistory(
            Long tradeId,
            TradeStatus fromStatus,
            TradeStatus toStatus,
            TradeStateTransitionReason reason
    ) {
        this.tradeId = tradeId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.changedAt = Instant.now();
    }

    public TradeStateHistory(
            Long tradeId,
            TradeStatus fromStatus,
            TradeStatus toStatus,
            TradeStateTransitionReason reason,
            Instant changedAt
    ) {
        this.tradeId = tradeId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.changedAt = changedAt;
    }
}
