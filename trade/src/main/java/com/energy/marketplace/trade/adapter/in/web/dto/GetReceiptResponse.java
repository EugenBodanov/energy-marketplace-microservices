package com.energy.marketplace.trade.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record GetReceiptResponse(
        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,
        @NotNull(message = "Receipt id must not be null")
        Long receiptId,
        @NotNull(message = "Buyer id must not be null")
        Long buyerId,
        @NotNull(message = "Seller id must not be null")
        Long sellerId,
        @NotNull(message = "Listing id must not be null")
        Long listingId,
        @NotNull(message = "Amount must not be null")
        com.energy.marketplace.trade.domain.valueobject.Money amount,
        @NotNull(message = "Generated at must not be null")
        Instant generatedAt
) {
}
