package com.energy.marketplace.trade.application.result;

import com.energy.marketplace.trade.domain.model.TradeStatus;

public record CreateTradeResult(
        Long tradeId,
        TradeStatus status
) {
}
