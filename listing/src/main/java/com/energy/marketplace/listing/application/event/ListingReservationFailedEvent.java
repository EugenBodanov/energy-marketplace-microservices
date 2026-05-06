package com.energy.marketplace.listing.application.event;

import java.time.Instant;

public record ListingReservationFailedEvent(
        Long listingId,
        Long tradeId,
        String reason,
        Instant timestamp
) {
    public static ListingReservationFailedEvent now(Long listingId, Long tradeId, String reason) {
        return new ListingReservationFailedEvent(listingId, tradeId, reason, Instant.now());
    }
}

