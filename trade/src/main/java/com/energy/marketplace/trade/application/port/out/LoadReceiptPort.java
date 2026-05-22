package com.energy.marketplace.trade.application.port.out;

import com.energy.marketplace.trade.application.result.GetReceiptResult;

public interface LoadReceiptPort {
    GetReceiptResult loadReceiptById(Long receiptId);
}