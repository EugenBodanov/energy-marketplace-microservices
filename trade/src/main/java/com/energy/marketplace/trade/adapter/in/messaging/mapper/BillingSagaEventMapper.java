package com.energy.marketplace.trade.adapter.in.messaging.mapper;

import com.energy.marketplace.trade.adapter.in.messaging.dto.PaymentAuthorizedEventMessage;
import com.energy.marketplace.trade.adapter.in.messaging.dto.PaymentSettledEventMessage;
import com.energy.marketplace.trade.adapter.in.messaging.dto.ReceiptGeneratedEventMessage;
import com.energy.marketplace.trade.application.command.in.HandlePaymentAuthorizedCommand;
import com.energy.marketplace.trade.application.command.in.HandlePaymentSettledCommand;
import com.energy.marketplace.trade.application.command.in.HandleReceiptGeneratedCommand;
import com.energy.marketplace.trade.domain.valueObject.Money;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class BillingSagaEventMapper {

    public HandlePaymentAuthorizedCommand toCommand(PaymentAuthorizedEventMessage event) {
        return new HandlePaymentAuthorizedCommand(
                event.tradeId(),
                event.paymentAuthorizationId(),
                new Money(
                        event.authorizedAmount(),
                        Currency.getInstance(event.currency())
                ),
                event.occurredAt()
        );
    }

    public HandlePaymentSettledCommand toCommand(PaymentSettledEventMessage event){
        return new HandlePaymentSettledCommand(
                event.tradeId(),
                event.paymentSettlementId(),
                new Money(
                        event.settledAmount(),
                        Currency.getInstance(event.currency())
                ),
                event.occurredAt()
        );
    }

    public HandleReceiptGeneratedCommand toCommand(ReceiptGeneratedEventMessage event){
        return new HandleReceiptGeneratedCommand(
                event.tradeId(), event.receiptId(), event.occurredAt()
        );
    }
}