package com.energy.marketplace.trade.application.exception;

public class TradeSagaProcessingException extends RuntimeException {

    public TradeSagaProcessingException(String message) {
        super(message);
    }

    public TradeSagaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
