package com.energy.marketplace.listing.adapter.out.messaging;

import com.energy.marketplace.listing.application.event.*;
import com.energy.marketplace.listing.application.port.out.PublishListingEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListingEventPublisher implements PublishListingEventPort {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "trade.saga.exchange";
    private static final String LISTING_CREATED_ROUTING_KEY = "listing.created.event";
    private static final String LISTING_RESERVED_ROUTING_KEY = "listing.reserved.event";
    private static final String LISTING_RESERVATION_FAILED_ROUTING_KEY = "listing.reservation_failed.event";
    private static final String LISTING_RELEASED_ROUTING_KEY = "listing.released.event";
    private static final String LISTING_CLOSED_ROUTING_KEY = "listing.closed.event";

    @Override
    public void publishListingCreated(ListingCreatedEvent event) {
        log.info("Publishing ListingCreatedEvent for listing {}", event.listingId());
        rabbitTemplate.convertAndSend(EXCHANGE, LISTING_CREATED_ROUTING_KEY, event);
    }

    @Override
    public void publishListingReserved(ListingReservedEvent event) {
        log.info("Publishing ListingReservedEvent for listing {} and trade {}", event.listingId(), event.tradeId());
        rabbitTemplate.convertAndSend(EXCHANGE, LISTING_RESERVED_ROUTING_KEY, event);
    }

    @Override
    public void publishListingReservationFailed(ListingReservationFailedEvent event) {
        log.warn("Publishing ListingReservationFailedEvent for listing {} and trade {}: {}",
                event.listingId(), event.tradeId(), event.reason());
        rabbitTemplate.convertAndSend(EXCHANGE, LISTING_RESERVATION_FAILED_ROUTING_KEY, event);
    }

    @Override
    public void publishListingReleased(ListingReleasedEvent event) {
        log.info("Publishing ListingReleasedEvent for listing {} and trade {}", event.listingId(), event.tradeId());
        rabbitTemplate.convertAndSend(EXCHANGE, LISTING_RELEASED_ROUTING_KEY, event);
    }

    @Override
    public void publishListingClosed(ListingClosedEvent event) {
        log.info("Publishing ListingClosedEvent for listing {} and trade {}", event.listingId(), event.tradeId());
        rabbitTemplate.convertAndSend(EXCHANGE, LISTING_CLOSED_ROUTING_KEY, event);
    }
}

