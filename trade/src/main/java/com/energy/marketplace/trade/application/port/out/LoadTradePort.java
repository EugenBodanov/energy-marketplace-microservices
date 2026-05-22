package com.energy.marketplace.trade.application.port.out;

import com.energy.marketplace.trade.domain.model.Trade;

import java.util.List;

public interface LoadTradePort {
    Trade loadTrade(Long tradeId);
    List<Trade> loadTradesByBuyerId (Long buyerId);
}
