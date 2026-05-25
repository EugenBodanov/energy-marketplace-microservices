package com.energy.marketplace.trade.application.command.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record HandlePaymentSettlementFailedCommand(
        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,

        @NotNull(message = "Payment authorization id must not be null")
        @Positive(message = "Payment authorization id must be positive")
        Long paymentAuthorizationId
) {
}
