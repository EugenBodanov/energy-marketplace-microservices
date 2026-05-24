package com.energy.marketplace.trade.adapter.in.messaging.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentSettledEventMessage (
        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,

        @NotNull(message = "Payment settlement id must not be null")
        @Positive(message = "Payment settlement id must be positive")
        Long paymentSettlementId,

        @NotNull(message = "Settled amount must not be null")
        @Positive(message = "Settled amount must be positive")
        BigDecimal settledAmount,

        @NotNull(message = "Currency must not be null")
        String currency,

        @NotNull(message = "Occurred at must not be null")
        Instant occurredAt
) {
}
