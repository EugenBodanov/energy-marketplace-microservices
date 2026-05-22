package com.energy.marketplace.trade.adapter.in.web.dto;

import com.energy.marketplace.trade.domain.model.TradeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTradeResponse(
        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,
        @NotNull(message = "Trade status must not be null")
        TradeStatus status
) {
}
