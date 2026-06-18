package com.energy.marketplace.shared.messaging;

public final class ListingSagaMessaging {

    public static final String EXCHANGE = "trade.saga.exchange";

    public static final String RESERVE_LISTING_COMMAND = "listing.reserve.command";
    public static final String CLOSE_LISTING_COMMAND = "listing.close.command";
    public static final String CANCEL_LISTING_COMMAND = "listing.cancel.command";

    public static final String LISTING_CREATED_EVENT = "listing.created.event";
    public static final String LISTING_RESERVED_EVENT = "listing.reserved.event";
    public static final String LISTING_RESERVATION_FAILED_EVENT = "listing.reservation_failed.event";
    public static final String LISTING_CLOSED_EVENT = "listing.closed.event";
    public static final String LISTING_CLOSE_FAILED_EVENT = "listing.close_failed.event";
    public static final String LISTING_COMPENSATION_SUCCEEDED_EVENT = "listing.compensation_succeeded.event";
    public static final String LISTING_COMPENSATION_FAILED_EVENT = "listing.compensation_failed.event";

    public static final String LISTING_COMMANDS_QUEUE = "listing.commands.queue";
    public static final String LISTING_EVENTS_QUEUE = "listing.events.queue";

    private ListingSagaMessaging() {
    }

    public static String[] getAllCommandKeys() {
        return new String[]{
                RESERVE_LISTING_COMMAND,
                CLOSE_LISTING_COMMAND,
                CANCEL_LISTING_COMMAND
        };
    }

    public static String[] getAllEventKeys() {
        return new String[]{
                LISTING_RESERVED_EVENT,
                LISTING_RESERVATION_FAILED_EVENT,
                LISTING_CLOSED_EVENT,
                LISTING_CLOSE_FAILED_EVENT,
                LISTING_COMPENSATION_SUCCEEDED_EVENT,
                LISTING_COMPENSATION_FAILED_EVENT
        };
    }
}
