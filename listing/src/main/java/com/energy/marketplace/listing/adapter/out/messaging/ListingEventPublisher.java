package com.energy.marketplace.listing.adapter.out.messaging;

import com.energy.marketplace.shared.messaging.ListingSagaMessaging;
import com.energy.marketplace.listing.adapter.out.messaging.dto.*;
import com.energy.marketplace.listing.application.event.*;
import com.energy.marketplace.listing.application.port.out.PublishListingEventPort;
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
public class ListingEventPublisher implements PublishListingEventPort {

    private final RabbitTemplate rabbitTemplate;
    private final JsonMapper jsonMapper;

    @Override
    public void publishListingCreated(ListingCreatedEvent event) {
        log.info("Publishing ListingCreatedEvent for listing {}", event.listingId());
        sendJson(ListingSagaMessaging.LISTING_CREATED_EVENT, event);
    }

    @Override
    public void publishListingReserved(ListingReservedEvent event) {
        log.info("Publishing ListingReservedEvent for listing {} and trade {}", event.listingId(), event.tradeId());
        ListingReservedEventMessage message = new ListingReservedEventMessage(
                "LISTING_RESERVED",
                event.listingId(),
                event.tradeId(),
                event.timestamp()
        );
        sendJson(
                ListingSagaMessaging.LISTING_RESERVED_EVENT,
                message
        );
    }

    @Override
    public void publishListingReservationFailed(ListingReservationFailedEvent event) {
        log.warn("Publishing ListingReservationFailedEvent for listing {} and trade {}: {}",
                event.listingId(), event.tradeId(), event.reason());
        ListingReservationFailedEventMessage message = new ListingReservationFailedEventMessage(
                "LISTING_RESERVATION_FAILED",
                event.listingId(),
                event.tradeId(),
                event.reason(),
                event.timestamp()
        );
        sendJson(
                ListingSagaMessaging.LISTING_RESERVATION_FAILED_EVENT,
                message
        );
    }

    @Override
    public void publishListingReleased(ListingReleasedEvent event) {
        log.info("Publishing ListingReleasedEvent (mapped to compensation) for listing {} and trade {}", 
                event.listingId(), event.tradeId());
        ListingCompensationSucceededEventMessage message = new ListingCompensationSucceededEventMessage(
                "LISTING_COMPENSATION_SUCCEEDED",
                event.listingId(),
                event.tradeId(),
                event.timestamp()
        );
        sendJson(
                ListingSagaMessaging.LISTING_COMPENSATION_SUCCEEDED_EVENT,
                message
        );
    }

    @Override
    public void publishListingClosed(ListingClosedEvent event) {
        log.info("Publishing ListingClosedEvent for listing {} and trade {}", event.listingId(), event.tradeId());
        ListingClosedEventMessage message = new ListingClosedEventMessage(
                "LISTING_CLOSED",
                event.listingId(),
                event.tradeId(),
                event.timestamp()
        );
        sendJson(
                ListingSagaMessaging.LISTING_CLOSED_EVENT,
                message
        );
    }

    @Override
    public void publishListingCloseFailed(ListingCloseFailedEvent event) {
        log.warn("Publishing ListingCloseFailedEvent for listing {} and trade {}: {}",
                event.listingId(), event.tradeId(), event.reason());
        ListingCloseFailedEventMessage message = new ListingCloseFailedEventMessage(
                "LISTING_CLOSE_FAILED",
                event.listingId(),
                event.tradeId(),
                event.reason(),
                event.timestamp()
        );
        sendJson(
                ListingSagaMessaging.LISTING_CLOSE_FAILED_EVENT,
                message
        );
    }

    @Override
    public void publishListingCancelled(ListingCancelledEvent event) {
        log.info("Publishing ListingCancelledEvent (compensation succeeded) for listing {} and trade {}", 
                event.listingId(), event.tradeId());
        ListingCompensationSucceededEventMessage message = new ListingCompensationSucceededEventMessage(
                "LISTING_COMPENSATION_SUCCEEDED",
                event.listingId(),
                event.tradeId(),
                event.timestamp()
        );
        sendJson(
                ListingSagaMessaging.LISTING_COMPENSATION_SUCCEEDED_EVENT,
                message
        );
    }

    @Override
    public void publishListingCompensationFailed(ListingCompensationFailedEvent event) {
        log.warn("Publishing ListingCompensationFailedEvent for listing {} and trade {}: {}",
                event.listingId(), event.tradeId(), event.reason());
        ListingCompensationFailedEventMessage message = new ListingCompensationFailedEventMessage(
                "LISTING_COMPENSATION_FAILED",
                event.listingId(),
                event.tradeId(),
                event.reason(),
                event.timestamp()
        );
        sendJson(
                ListingSagaMessaging.LISTING_COMPENSATION_FAILED_EVENT,
                message
        );
    }

    private void sendJson(String routingKey, Object payload) {
        try {
            Message message = MessageBuilder
                    .withBody(jsonMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8))
                    .setContentType("application/json")
                    .setContentEncoding(StandardCharsets.UTF_8.name())
                    .build();
            rabbitTemplate.send(ListingSagaMessaging.EXCHANGE, routingKey, message);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize listing saga event", exception);
        }
    }
}

