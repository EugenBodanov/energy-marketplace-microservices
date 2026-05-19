package com.energy.marketplace.trade.application.port.out;

import com.energy.marketplace.trade.domain.model.TradeStateHistory;

public interface SaveTradeStateHistoryPort {
    TradeStateHistory save(TradeStateHistory tradeStateHistory);
}
