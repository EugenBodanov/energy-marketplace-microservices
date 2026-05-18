package com.energy.marketplace.trade.application.port.in;

import com.energy.marketplace.trade.application.command.in.HandlePaymentAuthorizedCommand;
import com.energy.marketplace.trade.application.command.in.HandlePaymentSettledCommand;
import com.energy.marketplace.trade.application.command.in.HandleReceiptGeneratedCommand;

public interface HandleBillingSagaEventUseCase {
    void handlePaymentAuthorized(HandlePaymentAuthorizedCommand command);
    void handlePaymentSettled(HandlePaymentSettledCommand command);
    void handleReceiptGenerated(HandleReceiptGeneratedCommand command);
}
