package com.energy.marketplace.trade.application.command.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GetReceiptCommand(

        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId
) {
}
