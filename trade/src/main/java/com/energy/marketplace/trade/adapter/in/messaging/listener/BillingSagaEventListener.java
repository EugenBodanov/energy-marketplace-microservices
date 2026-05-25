package com.energy.marketplace.trade.adapter.in.messaging.listener;

import com.energy.marketplace.trade.adapter.in.messaging.dto.*;
import com.energy.marketplace.trade.adapter.in.messaging.mapper.BillingSagaEventMapper;
import com.energy.marketplace.trade.application.service.HandleBillingSagaEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class BillingSagaEventListener extends Listener{

    private final ObjectMapper objectMapper;
    private final HandleBillingSagaEventService billingSagaEventService;
    private final BillingSagaEventMapper billingSagaEventMapper;

    @RabbitListener(queues = "${trade.billing-events.queue}")
    public void handleEvent(String event) {
        try {
            JsonNode eventNode = objectMapper.readTree(event);
            String eventType = super.readEventType(eventNode);
            switch (eventType) {
                case "PAYMENT_AUTHORIZED" -> {
                    PaymentAuthorizedEventMessage eventMessage = objectMapper.readValue(event, PaymentAuthorizedEventMessage.class);
                    billingSagaEventService.handlePaymentAuthorized(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            ));
                }
                case "PAYMENT_SETTLED" -> {
                    PaymentSettledEventMessage eventMessage = objectMapper.readValue(event, PaymentSettledEventMessage.class);
                    billingSagaEventService.handlePaymentSettled(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "RECEIPT_GENERATED" -> {
                    ReceiptGeneratedEventMessage eventMessage = objectMapper.readValue(event, ReceiptGeneratedEventMessage.class);
                    billingSagaEventService.handleReceiptGenerated(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "PAYMENT_AUTHORIZATION_FAILED" -> {
                    PaymentAuthorizationFailedEventMessage eventMessage = objectMapper.readValue(event, PaymentAuthorizationFailedEventMessage.class);
                    billingSagaEventService.handlePaymentAuthorizationFailed(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "PAYMENT_SETTLEMENT_FAILED" -> {
                    PaymentSettlementFailedEventMessage eventMessage = objectMapper.readValue(event, PaymentSettlementFailedEventMessage.class);
                    billingSagaEventService.handlePaymentSettlementFailed(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "CANCEL_PAYMENT_SUCCESS" -> {
                    CancelPaymentSuccessEventMessage eventMessage = objectMapper.readValue(event, CancelPaymentSuccessEventMessage.class);
                    billingSagaEventService.handleCancelPaymentSuccess(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "CANCEL_PAYMENT_FAILED" -> {
                    CancelPaymentFailedEventMessage eventMessage = objectMapper.readValue(event, CancelPaymentFailedEventMessage.class);
                    billingSagaEventService.handleCancelPaymentFailed(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                default -> throw new IllegalArgumentException(
                        "Unknown billing event type: " + eventType
                );
            }
        } catch (JsonProcessingException | IllegalArgumentException e){
            log.error("Failed to handle billing saga event: {}", event, e);
        }
    }
}
