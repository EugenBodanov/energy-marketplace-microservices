package com.energy.marketplace.trade.application.port.out;

import com.energy.marketplace.trade.domain.model.Trade;

public interface LoadTradePort {
    Trade load(Long tradeId);
}
