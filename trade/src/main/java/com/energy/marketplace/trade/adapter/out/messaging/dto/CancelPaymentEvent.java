package com.energy.marketplace.trade.adapter.out.messaging.dto;

import com.energy.marketplace.trade.domain.valueObject.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record CancelPaymentEvent (
        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,

        @NotNull(message = "Payment authorization id must not be null")
        @Positive(message = "Payment authorization id must be positive")
        Long paymentAuthorizationId,

        @NotNull(message = "Amount must not be null")
        @Positive(message = "Amount must be positive")
        Money amount,

        @NotNull(message = "Requested at must not be null")
        Instant requestedAt
) {
}
