package com.energy.marketplace.trade.adapter.out.messaging.mapper;

import com.energy.marketplace.trade.adapter.out.messaging.dto.AuthorizePaymentEvent;
import com.energy.marketplace.trade.adapter.out.messaging.dto.CancelPaymentEvent;
import com.energy.marketplace.trade.adapter.out.messaging.dto.GenerateReceiptEvent;
import com.energy.marketplace.trade.adapter.out.messaging.dto.SettlePaymentEvent;
import com.energy.marketplace.trade.application.command.out.AuthorizePaymentCommand;
import com.energy.marketplace.trade.application.command.out.CancelPaymentCommand;
import com.energy.marketplace.trade.application.command.out.GenerateReceiptCommand;
import com.energy.marketplace.trade.application.command.out.SettlePaymentCommand;
import org.springframework.stereotype.Component;

@Component
public class BillingMessagingMapper {

    public AuthorizePaymentEvent toEvent(AuthorizePaymentCommand command) {
        return new AuthorizePaymentEvent(
                command.tradeId(),
                command.buyerId(),
                command.sellerId(),
                command.amount().amount(),
                command.amount().currencyCode(),
                command.requestedAt()
        );
    }

    public SettlePaymentEvent toEvent(SettlePaymentCommand command) {
        return new SettlePaymentEvent(
                command.tradeId(),
                command.paymentAuthorizationId(),
                command.amount().amount(),
                command.amount().currencyCode(),
                command.requestedAt()
        );
    }

    public GenerateReceiptEvent toEvent(GenerateReceiptCommand command) {
        return new GenerateReceiptEvent(
                command.tradeId(),
                command.buyerId(),
                command.sellerId(),
                command.listingId(),
                command.amount().amount(),
                command.amount().currencyCode(),
                command.tradeCompletedAt()
        );
    }

    public CancelPaymentEvent toEvent(CancelPaymentCommand command) {
        return new CancelPaymentEvent(command.tradeId(), command.paymentAuthorizationId(), command.amount(), command.requestedAt());
    }
}
