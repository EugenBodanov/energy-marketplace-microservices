package com.energy.marketplace.trade.adapter.out.web.exception;

public class ReceiptNotFoundException extends RuntimeException {

    public ReceiptNotFoundException(Long tradeId) {
        super("Receipt was not found for tradeId=%d".formatted(tradeId));
    }
}
