package com.energy.marketplace.trade.application.port.in;

import com.energy.marketplace.trade.application.command.in.GetReceiptCommand;
import com.energy.marketplace.trade.application.result.GetReceiptResult;

public interface GetReceiptUseCase {
    GetReceiptResult getReceipt(GetReceiptCommand command);
}
