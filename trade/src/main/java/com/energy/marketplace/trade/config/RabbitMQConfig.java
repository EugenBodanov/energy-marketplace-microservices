package com.energy.marketplace.trade.config;

import com.energy.marketplace.shared.messaging.ListingSagaMessaging;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${trade.listing-events.queue:trade.listing.events.queue}")
    private String listingEventsQueueName;

    @Value("${trade.billing-events.queue:trade.billing.events.queue}")
    private String billingEventsQueueName;

    // Routing keys for billing events
    public static final String PAYMENT_AUTHORIZED_ROUTING_KEY = "payment.authorized.event";
    public static final String PAYMENT_AUTHORIZATION_FAILED_ROUTING_KEY = "payment.authorization_failed.event";
    public static final String PAYMENT_SETTLED_ROUTING_KEY = "payment.settled.event";
    public static final String PAYMENT_SETTLEMENT_FAILED_ROUTING_KEY = "payment.settlement_failed.event";
    public static final String PAYMENT_ROLLED_BACK_ROUTING_KEY = "payment.rolled_back.event";
    public static final String RECEIPT_GENERATED_ROUTING_KEY = "receipt.generated.event";
    public static final String RECEIPT_GENERATION_FAILED_ROUTING_KEY = "receipt.generation_failed.event";

    @Bean
    public DirectExchange tradeExchange() {
        return new DirectExchange(ListingSagaMessaging.EXCHANGE, true, false);
    }

    @Bean
    public Queue listingEventsQueue() {
        return new Queue(listingEventsQueueName, true);
    }

    @Bean
    public Queue billingEventsQueue() {
        return new Queue(billingEventsQueueName, true);
    }

    // ==================== LISTING EVENT BINDINGS ====================
    @Bean
    public Binding bindingListingReserved(DirectExchange tradeExchange, Queue listingEventsQueue) {
        return BindingBuilder.bind(listingEventsQueue).to(tradeExchange).with(ListingSagaMessaging.LISTING_RESERVED_EVENT);
    }

    @Bean
    public Binding bindingListingReservationFailed(DirectExchange tradeExchange, Queue listingEventsQueue) {
        return BindingBuilder.bind(listingEventsQueue).to(tradeExchange).with(ListingSagaMessaging.LISTING_RESERVATION_FAILED_EVENT);
    }

    @Bean
    public Binding bindingListingClosed(DirectExchange tradeExchange, Queue listingEventsQueue) {
        return BindingBuilder.bind(listingEventsQueue).to(tradeExchange).with(ListingSagaMessaging.LISTING_CLOSED_EVENT);
    }

    @Bean
    public Binding bindingListingCloseFailed(DirectExchange tradeExchange, Queue listingEventsQueue) {
        return BindingBuilder.bind(listingEventsQueue).to(tradeExchange).with(ListingSagaMessaging.LISTING_CLOSE_FAILED_EVENT);
    }

    @Bean
    public Binding bindingListingCompensationSucceeded(DirectExchange tradeExchange, Queue listingEventsQueue) {
        return BindingBuilder.bind(listingEventsQueue).to(tradeExchange).with(ListingSagaMessaging.LISTING_COMPENSATION_SUCCEEDED_EVENT);
    }

    @Bean
    public Binding bindingListingCompensationFailed(DirectExchange tradeExchange, Queue listingEventsQueue) {
        return BindingBuilder.bind(listingEventsQueue).to(tradeExchange).with(ListingSagaMessaging.LISTING_COMPENSATION_FAILED_EVENT);
    }

    // ==================== BILLING EVENT BINDINGS ====================
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
    public Binding bindingReceiptGenerationFailed(DirectExchange tradeExchange, Queue billingEventsQueue) {
        return BindingBuilder.bind(billingEventsQueue).to(tradeExchange).with(RECEIPT_GENERATION_FAILED_ROUTING_KEY);
    }

}
