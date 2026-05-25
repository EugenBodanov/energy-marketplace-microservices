package com.energy.marketplace.trade.application.port.out;

import com.energy.marketplace.trade.application.command.out.AuthorizePaymentCommand;
import com.energy.marketplace.trade.application.command.out.CancelPaymentCommand;
import com.energy.marketplace.trade.application.command.out.GenerateReceiptCommand;
import com.energy.marketplace.trade.application.command.out.SettlePaymentCommand;

public interface SendBillingCommandPort {

    void authorizePayment(AuthorizePaymentCommand command);

    void settlePayment(SettlePaymentCommand command);

    void generateReceipt(GenerateReceiptCommand command);

    void cancelPayment(CancelPaymentCommand command);


}