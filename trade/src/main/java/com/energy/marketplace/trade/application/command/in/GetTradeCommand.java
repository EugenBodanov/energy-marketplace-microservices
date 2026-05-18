package com.energy.marketplace.trade.application.command.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GetTradeCommand(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be positive")
        Long tradeId
) {
}
