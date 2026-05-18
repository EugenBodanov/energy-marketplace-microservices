package com.energy.marketplace.trade.application.command.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import com.energy.marketplace.trade.domain.valueobject.Money;

public record HandlePaymentAuthorizedCommand(

        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,

        @NotNull(message = "Payment authorization id must not be null")
        @Positive(message = "Payment authorization id must be positive")
        Long paymentAuthorizationId,

        @NotNull(message = "Authorized amount must not be null")
        @Positive(message = "Authorized amount must be positive")
        Money authorizedAmount,

        @NotNull(message = "Occurred at must not be null")
        Instant occurredAt
) {
}
