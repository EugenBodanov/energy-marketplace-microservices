package com.energy.marketplace.trade.application.port.out;

import com.energy.marketplace.trade.domain.model.Trade;

public interface SaveTradePort {
    Trade save(Trade trade);
}
