package com.energy.marketplace.trade.adapter.out.messaging.sender;

import com.energy.marketplace.trade.adapter.out.messaging.mapper.BillingMessagingMapper;
import com.energy.marketplace.trade.application.command.out.AuthorizePaymentCommand;
import com.energy.marketplace.trade.application.command.out.CancelPaymentCommand;
import com.energy.marketplace.trade.application.command.out.GenerateReceiptCommand;
import com.energy.marketplace.trade.application.command.out.SettlePaymentCommand;
import com.energy.marketplace.trade.application.port.out.SendBillingCommandPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingSagaEventSender implements SendBillingCommandPort {

    private final RabbitTemplate rabbitTemplate;
    private final BillingMessagingMapper mapper;
    private final JsonMapper jsonMapper;

    private static final String EXCHANGE = "trade.saga.exchange";
    private static final String AUTHORIZE_ROUTING_KEY = "billing.authorize.command";
    private static final String SETTLE_ROUTING_KEY = "billing.settle.command";
    private static final String RECEIPT_ROUTING_KEY = "billing.generate_receipt.command";
    private static final String CANCEL_PAYMENT_ROUTING_KEY = "billing.cancel_payment.command";

    @Override
    public void authorizePayment(AuthorizePaymentCommand command) {
        log.info("Sending authorize payment command for trade: {}", command.tradeId());
        sendJson(AUTHORIZE_ROUTING_KEY, mapper.toEvent(command));
    }

    @Override
    public void settlePayment(SettlePaymentCommand command) {
        log.info("Sending settle payment command for trade: {}", command.tradeId());
        sendJson(SETTLE_ROUTING_KEY, mapper.toEvent(command));
    }

    @Override
    public void generateReceipt(GenerateReceiptCommand command) {
        log.info("Sending generate receipt command for trade: {}", command.tradeId());
        sendJson(RECEIPT_ROUTING_KEY, mapper.toEvent(command));
    }

    @Override
    public void cancelPayment(CancelPaymentCommand command) {
        log.info("Sending cancel payment command for trade: {}", command.tradeId());
        sendJson(CANCEL_PAYMENT_ROUTING_KEY, mapper.toEvent(command));
    }

    private void sendJson(String routingKey, Object payload) {
        try {
            Message message = MessageBuilder
                    .withBody(jsonMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8))
                    .setContentType("application/json")
                    .setContentEncoding(StandardCharsets.UTF_8.name())
                    .build();
            rabbitTemplate.send(EXCHANGE, routingKey, message);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize billing saga message", exception);
        }
    }

}
