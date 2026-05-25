package com.energy.marketplace.trade.application.command.out;

import com.energy.marketplace.trade.domain.valueObject.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record CancelPaymentCommand (
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
        public static CancelPaymentCommand of(
                Long tradeId,
                Long paymentAuthorizationId,
                Money amount
        ) {
                return new CancelPaymentCommand(
                        tradeId,
                        paymentAuthorizationId,
                        amount,
                        Instant.now()
                );
        }
}
