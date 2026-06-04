package com.energy.marketplace.trade.adapter.in.messaging.listener;

import com.energy.marketplace.trade.adapter.in.messaging.dto.*;
import com.energy.marketplace.trade.adapter.in.messaging.mapper.BillingSagaEventMapper;
import com.energy.marketplace.trade.application.service.HandleBillingSagaEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;


@Slf4j
@Component
@RequiredArgsConstructor
public class BillingSagaEventListener extends Listener{

    private final JsonMapper jsonMapper;
    private final HandleBillingSagaEventService billingSagaEventService;
    private final BillingSagaEventMapper billingSagaEventMapper;

    @RabbitListener(queues = "${trade.billing-events.queue}")
    public void handleEvent(String event) {
        try {
            JsonNode eventNode = jsonMapper.readTree(event);
            String eventType = super.readEventType(eventNode);
            switch (eventType) {
                case "PAYMENT_AUTHORIZED" -> {
                    PaymentAuthorizedEventMessage eventMessage = jsonMapper.readValue(event, PaymentAuthorizedEventMessage.class);
                    billingSagaEventService.handlePaymentAuthorized(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            ));
                }
                case "PAYMENT_SETTLED" -> {
                    PaymentSettledEventMessage eventMessage = jsonMapper.readValue(event, PaymentSettledEventMessage.class);
                    billingSagaEventService.handlePaymentSettled(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "RECEIPT_GENERATED" -> {
                    ReceiptGeneratedEventMessage eventMessage = jsonMapper.readValue(event, ReceiptGeneratedEventMessage.class);
                    billingSagaEventService.handleReceiptGenerated(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "RECEIPT_GENERATION_FAILED" -> {
                    ReceiptGenerationFailedEventMessage eventMessage = jsonMapper.readValue(event, ReceiptGenerationFailedEventMessage.class);
                    billingSagaEventService.handleReceiptGenerationFailed(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "PAYMENT_AUTHORIZATION_FAILED" -> {
                    PaymentAuthorizationFailedEventMessage eventMessage = jsonMapper.readValue(event, PaymentAuthorizationFailedEventMessage.class);
                    billingSagaEventService.handlePaymentAuthorizationFailed(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "PAYMENT_SETTLEMENT_FAILED" -> {
                    PaymentSettlementFailedEventMessage eventMessage = jsonMapper.readValue(event, PaymentSettlementFailedEventMessage.class);
                    billingSagaEventService.handlePaymentSettlementFailed(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "CANCEL_PAYMENT_SUCCESS" -> {
                    CancelPaymentSuccessEventMessage eventMessage = jsonMapper.readValue(event, CancelPaymentSuccessEventMessage.class);
                    billingSagaEventService.handleCancelPaymentSuccess(
                            billingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                case "CANCEL_PAYMENT_FAILED" -> {
                    CancelPaymentFailedEventMessage eventMessage = jsonMapper.readValue(event, CancelPaymentFailedEventMessage.class);
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
        } catch (JacksonException | IllegalArgumentException e){
            log.error("Failed to handle billing saga event: {}", event, e);
        }
    }
}
