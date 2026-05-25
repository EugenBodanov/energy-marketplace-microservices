package com.energy.marketplace.trade.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "trade.saga.exchange";

    @Value("${trade.listing-events.queue:trade.listing.events.queue}")
    private String listingEventsQueueName;

    @Value("${trade.billing-events.queue:trade.billing.events.queue}")
    private String billingEventsQueueName;

    // Routing keys for events
    public static final String LISTING_RESERVED_ROUTING_KEY = "listing.reserved.event";
    public static final String LISTING_RESERVATION_FAILED_ROUTING_KEY = "listing.reservation_failed.event";
    public static final String LISTING_RELEASED_ROUTING_KEY = "listing.released.event";
    public static final String LISTING_CLOSED_ROUTING_KEY = "listing.closed.event";

    public static final String PAYMENT_AUTHORIZED_ROUTING_KEY = "payment.authorized.event";
    public static final String PAYMENT_AUTHORIZATION_FAILED_ROUTING_KEY = "payment.authorization_failed.event";
    public static final String PAYMENT_SETTLED_ROUTING_KEY = "payment.settled.event";
    public static final String PAYMENT_SETTLEMENT_FAILED_ROUTING_KEY = "payment.settlement_failed.event";
    public static final String PAYMENT_ROLLED_BACK_ROUTING_KEY = "payment.rolled_back.event";
    public static final String RECEIPT_GENERATED_ROUTING_KEY = "receipt.generated.event";

    @Bean
    public DirectExchange tradeExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue listingEventsQueue() {
        return new Queue(listingEventsQueueName, true);
    }

    @Bean
    public Queue billingEventsQueue() {
        return new Queue(billingEventsQueueName, true);
    }

    // Bindings for listing events
    @Bean
    public Binding bindingListingReserved(DirectExchange tradeExchange, Queue listingEventsQueue) {
        return BindingBuilder.bind(listingEventsQueue).to(tradeExchange).with(LISTING_RESERVED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingListingReservationFailed(DirectExchange tradeExchange, Queue listingEventsQueue) {
        return BindingBuilder.bind(listingEventsQueue).to(tradeExchange).with(LISTING_RESERVATION_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingListingReleased(DirectExchange tradeExchange, Queue listingEventsQueue) {
        return BindingBuilder.bind(listingEventsQueue).to(tradeExchange).with(LISTING_RELEASED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingListingClosed(DirectExchange tradeExchange, Queue listingEventsQueue) {
        return BindingBuilder.bind(listingEventsQueue).to(tradeExchange).with(LISTING_CLOSED_ROUTING_KEY);
    }

    // Bindings for billing events
    @Bean
    public Binding bindingPaymentAuthorized(DirectExchange tradeExchange, Queue billingEventsQueue) {
        return BindingBuilder.bind(billingEventsQueue).to(tradeExchange).with(PAYMENT_AUTHORIZED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingPaymentAuthorizationFailed(DirectExchange tradeExchange, Queue billingEventsQueue) {
        return BindingBuilder.bind(billingEventsQueue).to(tradeExchange).with(PAYMENT_AUTHORIZATION_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingPaymentSettled(DirectExchange tradeExchange, Queue billingEventsQueue) {
        return BindingBuilder.bind(billingEventsQueue).to(tradeExchange).with(PAYMENT_SETTLED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingPaymentSettlementFailed(DirectExchange tradeExchange, Queue billingEventsQueue) {
        return BindingBuilder.bind(billingEventsQueue).to(tradeExchange).with(PAYMENT_SETTLEMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingPaymentRolledBack(DirectExchange tradeExchange, Queue billingEventsQueue) {
        return BindingBuilder.bind(billingEventsQueue).to(tradeExchange).with(PAYMENT_ROLLED_BACK_ROUTING_KEY);
    }

    @Bean
    public Binding bindingReceiptGenerated(DirectExchange tradeExchange, Queue billingEventsQueue) {
        return BindingBuilder.bind(billingEventsQueue).to(tradeExchange).with(RECEIPT_GENERATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
