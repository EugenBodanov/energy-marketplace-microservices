package com.energy.marketplace.trade.adapter.in.messaging.listener;

import com.energy.marketplace.trade.adapter.in.messaging.dto.*;
import com.energy.marketplace.trade.adapter.in.messaging.mapper.ListingSagaEventMapper;
import com.energy.marketplace.trade.application.service.HandleListingSagaEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingSagaEventListener extends Listener{

    private final ObjectMapper objectMapper;
    private final HandleListingSagaEventService listingSagaEventService;
    private final ListingSagaEventMapper listingSagaEventMapper;

    @RabbitListener(queues = "${trade.listing-events.queue}")
    public void handleEvent(String event) {
        try {
            JsonNode eventNode = objectMapper.readTree(event);
            String eventType = super.readEventType(eventNode);
            switch (eventType) {
                case "LISTING_RESERVED" -> {
                    ListingReservedEventMessage eventMessage = objectMapper.readValue(event, ListingReservedEventMessage.class);
                    listingSagaEventService.handleListingReserved(
                            listingSagaEventMapper.toCommand(
                                    eventMessage
                            ));
                }
                case "LISTING_CLOSED" -> {
                    ListingClosedEventMessage eventMessage = objectMapper.readValue(event, ListingClosedEventMessage.class);
                    listingSagaEventService.handleListingClosed(
                            listingSagaEventMapper.toCommand(
                                    eventMessage
                            )
                    );
                }
                default -> throw new IllegalArgumentException(
                        "Unknown listing event type: " + eventType
                );
            }
        } catch (JsonProcessingException | IllegalArgumentException e){
            log.error("Failed to handle listing saga event: {}", event, e);
        }
    }
}
