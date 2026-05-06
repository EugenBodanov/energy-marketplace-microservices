package com.energy.marketplace.listing.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE = "trade.saga.exchange";

    // Queues
    public static final String LISTING_COMMANDS_QUEUE = "listing.commands.queue";
    public static final String LISTING_EVENTS_QUEUE = "listing.events.queue";

    // Routing keys
    public static final String RESERVE_LISTING_ROUTING_KEY = "listing.reserve.command";
    public static final String RELEASE_LISTING_ROUTING_KEY = "listing.release.command";
    public static final String CLOSE_LISTING_ROUTING_KEY = "listing.close.command";
    public static final String LISTING_CREATED_ROUTING_KEY = "listing.created.event";
    public static final String LISTING_RESERVED_ROUTING_KEY = "listing.reserved.event";
    public static final String LISTING_RESERVATION_FAILED_ROUTING_KEY = "listing.reservation_failed.event";
    public static final String LISTING_RELEASED_ROUTING_KEY = "listing.released.event";
    public static final String LISTING_CLOSED_ROUTING_KEY = "listing.closed.event";

    @Bean
    public DirectExchange tradeExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue listingCommandsQueue() {
        return new Queue(LISTING_COMMANDS_QUEUE, true);
    }

    @Bean
    public Queue listingEventsQueue() {
        return new Queue(LISTING_EVENTS_QUEUE, true);
    }

    // Bindings for commands
    @Bean
    public Binding bindingReserveCommand() {
        return BindingBuilder.bind(listingCommandsQueue())
                .to(tradeExchange())
                .with(RESERVE_LISTING_ROUTING_KEY);
    }

    @Bean
    public Binding bindingReleaseCommand() {
        return BindingBuilder.bind(listingCommandsQueue())
                .to(tradeExchange())
                .with(RELEASE_LISTING_ROUTING_KEY);
    }

    @Bean
    public Binding bindingCloseCommand() {
        return BindingBuilder.bind(listingCommandsQueue())
                .to(tradeExchange())
                .with(CLOSE_LISTING_ROUTING_KEY);
    }

    // Bindings for events (if this service needs to listen to its own events for processing)
    @Bean
    public Binding bindingListingCreated() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(LISTING_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingListingReserved() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(LISTING_RESERVED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingListingReservationFailed() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(LISTING_RESERVATION_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingListingReleased() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(LISTING_RELEASED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingListingClosed() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(LISTING_CLOSED_ROUTING_KEY);
    }
}

