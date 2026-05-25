package com.energy.marketplace.trade.adapter.in.messaging.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record PaymentAuthorizationFailedEventMessage(
        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,

        @NotNull(message = "Payment authorization id must not be null")
        @Positive(message = "Payment authorization id must be positive")
        Long paymentAuthorizationId,

        @NotNull(message = "Occurred at must not be null")
        Instant occurredAt
) {
}
