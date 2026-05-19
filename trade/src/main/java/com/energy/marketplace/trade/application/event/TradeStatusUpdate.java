package com.energy.marketplace.trade.application.event;

import com.energy.marketplace.trade.domain.model.TradeStatus;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReasonCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public record TradeStatusUpdate(

        @NotNull(message = "Trade id must not be null")
        @PositiveOrZero(message = "Trade id must be positive or zero")
        Long tradeId,

        @NotNull(message = "Trade status must not be null")
        TradeStatus status,

        @NotNull(message = "Reason code must not be null")
        TradeStateTransitionReasonCode reasonCode,

        @NotNull(message = "Occurred at must not be null")
        Instant occurredAt
) {
    public static TradeStatusUpdate of(
            Long tradeId,
            TradeStatus status,
            TradeStateTransitionReasonCode reasonCode
    ) {
        return new TradeStatusUpdate(
                tradeId,
                status,
                reasonCode,
                Instant.now()
        );
    }
}
