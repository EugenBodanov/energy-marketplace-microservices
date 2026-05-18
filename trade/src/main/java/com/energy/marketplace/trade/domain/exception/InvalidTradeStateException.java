package com.energy.marketplace.trade.domain.exception;

import com.energy.marketplace.trade.domain.model.TradeStatus;

public class InvalidTradeStateException extends RuntimeException{
    public InvalidTradeStateException(String message) {
        super(message);
    }

    public InvalidTradeStateException(TradeStatus currentStatus, TradeStatus targetStatus) {
        super("Invalid trade state transition from " + currentStatus + " to " + targetStatus);
    }
}
