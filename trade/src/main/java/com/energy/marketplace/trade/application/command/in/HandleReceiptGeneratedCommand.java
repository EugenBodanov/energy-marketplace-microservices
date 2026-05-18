package com.energy.marketplace.trade.application.command.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record HandleReceiptGeneratedCommand(

        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,

        @NotNull(message = "Receipt id must not be null")
        @Positive(message = "Receipt id must be positive")
        Long receiptId,

        @NotNull(message = "Occurred at must not be null")
        Instant occurredAt
) {
}