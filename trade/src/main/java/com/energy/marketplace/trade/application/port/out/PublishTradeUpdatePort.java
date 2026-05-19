package com.energy.marketplace.trade.application.port.out;

import com.energy.marketplace.trade.application.event.TradeStatusUpdate;

public interface PublishTradeUpdatePort {
    void publishTradeStateUpdate(TradeStatusUpdate tradeStatusUpdate);
}
