package com.energy.marketplace.trade.application.exception;

public class ReceiptNotReadyException extends RuntimeException {

    public ReceiptNotReadyException(String message) {
        super(message);
    }

    public ReceiptNotReadyException(String message, Throwable cause) {
        super(message, cause);
    }
}