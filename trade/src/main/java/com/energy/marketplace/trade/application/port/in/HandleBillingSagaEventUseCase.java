package com.energy.marketplace.trade.application.port.in;

import com.energy.marketplace.trade.application.command.in.*;

public interface HandleBillingSagaEventUseCase {
    void handlePaymentAuthorized(HandlePaymentAuthorizedCommand command);
    void handlePaymentSettled(HandlePaymentSettledCommand command);
    void handlePaymentSettlementFailed(HandlePaymentSettlementFailedCommand command);
    void handleReceiptGenerated(HandleReceiptGeneratedCommand command);
    void handlePaymentAuthorizationFailed(HandlePaymentAuthorizationFailedCommand command);
    void handleCancelPaymentFailed(HandleCancelPaymentFailed command);
    void handleCancelPaymentSuccess(HandleCancelPaymentSuccess command);
}
