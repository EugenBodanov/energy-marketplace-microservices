package com.energy.marketplace.trade.adapter.in.messaging.mapper;

import com.energy.marketplace.trade.adapter.in.messaging.dto.*;
import com.energy.marketplace.trade.application.command.in.*;
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

    public HandleReceiptGenerationFailedCommand toCommand(ReceiptGenerationFailedEventMessage event){
        return new HandleReceiptGenerationFailedCommand(event.tradeId());
    }

    public HandlePaymentAuthorizationFailedCommand toCommand(PaymentAuthorizationFailedEventMessage event){
        return new HandlePaymentAuthorizationFailedCommand(event.tradeId(), event.paymentAuthorizationId(), event.occurredAt());
    }

    public HandlePaymentSettlementFailedCommand toCommand(PaymentSettlementFailedEventMessage event) {
        return new HandlePaymentSettlementFailedCommand(event.tradeId(), event.paymentAuthorizationId());
    }

    public HandleCancelPaymentSuccess toCommand(CancelPaymentSuccessEventMessage event) {
        return new HandleCancelPaymentSuccess(event.tradeId(), event.paymentAuthorizationId());
    }

    public HandleCancelPaymentFailed toCommand(CancelPaymentFailedEventMessage event) {
        return new HandleCancelPaymentFailed(event.tradeId(), event.paymentAuthorizationId());
    }
}