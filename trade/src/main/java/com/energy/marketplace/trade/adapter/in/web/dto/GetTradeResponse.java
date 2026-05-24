package com.energy.marketplace.trade.adapter.in.web.dto;

import com.energy.marketplace.trade.domain.model.TradeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record GetTradeResponse(
        @NotNull(message = "Id must not be null")
        @Positive(message = "Id must be positive")
        Long tradeId,

        @NotNull(message = "Buyer id must not be null")
        @Positive(message = "Buyer id must be positive")
        Long buyerId,

        @NotNull(message = "Seller id must not be null")
        @Positive(message = "Seller id must be positive")
        Long sellerId,

        @NotNull(message = "Listing id must not be null")
        @Positive(message = "Listing id must be positive")
        Long listingId,

        @NotNull(message = "Amount must not be null")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Currency must not be null")
        String currency,

        @NotNull(message = "Status must not be null")
        TradeStatus status
) {
}
