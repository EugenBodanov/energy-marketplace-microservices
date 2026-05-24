package com.energy.marketplace.trade.adapter.out.web.exception;

public class BillingReceiptClientException extends RuntimeException {

    public BillingReceiptClientException(String message) {
        super(message);
    }

    public BillingReceiptClientException(String message, Throwable cause) {
        super(message, cause);
    }
}