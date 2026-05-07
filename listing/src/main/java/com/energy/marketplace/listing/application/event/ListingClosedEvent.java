package com.energy.marketplace.listing.application.event;

import java.time.Instant;

public record ListingClosedEvent(
        Long listingId,
        Long tradeId,
        Instant timestamp
) {
    public static ListingClosedEvent now(Long listingId, Long tradeId) {
        return new ListingClosedEvent(listingId, tradeId, Instant.now());
    }
}

