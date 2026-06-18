package com.energy.marketplace.listing.config;

import com.energy.marketplace.shared.messaging.ListingSagaMessaging;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${listing.commands.queue:listing.commands.queue}")
    private String listingCommandsQueueName;

    @Value("${listing.events.queue:listing.events.queue}")
    private String listingEventsQueueName;

    @Bean
    public DirectExchange tradeExchange() {
        return new DirectExchange(ListingSagaMessaging.EXCHANGE, true, false);
    }

    @Bean
    public Queue listingCommandsQueue() {
        return new Queue(listingCommandsQueueName, true);
    }

    @Bean
    public Queue listingEventsQueue() {
        return new Queue(listingEventsQueueName, true);
    }

    // ==================== COMMAND BINDINGS ====================
    @Bean
    public Binding bindingReserveCommand() {
        return BindingBuilder.bind(listingCommandsQueue())
                .to(tradeExchange())
                .with(ListingSagaMessaging.RESERVE_LISTING_COMMAND);
    }

    @Bean
    public Binding bindingCloseCommand() {
        return BindingBuilder.bind(listingCommandsQueue())
                .to(tradeExchange())
                .with(ListingSagaMessaging.CLOSE_LISTING_COMMAND);
    }

    @Bean
    public Binding bindingCancelCommand() {
        return BindingBuilder.bind(listingCommandsQueue())
                .to(tradeExchange())
                .with(ListingSagaMessaging.CANCEL_LISTING_COMMAND);
    }

    // ==================== EVENT BINDINGS ====================
    @Bean
    public Binding bindingListingCreated() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with("listing.created.event");
    }

    @Bean
    public Binding bindingListingReserved() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(ListingSagaMessaging.LISTING_RESERVED_EVENT);
    }

    @Bean
    public Binding bindingListingReservationFailed() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(ListingSagaMessaging.LISTING_RESERVATION_FAILED_EVENT);
    }

    @Bean
    public Binding bindingListingClosed() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(ListingSagaMessaging.LISTING_CLOSED_EVENT);
    }

    @Bean
    public Binding bindingListingCloseFailed() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(ListingSagaMessaging.LISTING_CLOSE_FAILED_EVENT);
    }

    @Bean
    public Binding bindingListingCompensationSucceeded() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(ListingSagaMessaging.LISTING_COMPENSATION_SUCCEEDED_EVENT);
    }

    @Bean
    public Binding bindingListingCompensationFailed() {
        return BindingBuilder.bind(listingEventsQueue())
                .to(tradeExchange())
                .with(ListingSagaMessaging.LISTING_COMPENSATION_FAILED_EVENT);
    }
}
