package com.energy.marketplace.trade.application.port.out;

import com.energy.marketplace.trade.application.result.GetReceiptResult;

public interface RequestReceiptPort {
    GetReceiptResult requestReceiptById(Long receiptId);
}