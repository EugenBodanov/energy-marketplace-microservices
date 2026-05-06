package com.energy.marketplace.listing.application.event;

import java.time.Instant;

public record ListingReleasedEvent(
        Long listingId,
        Long tradeId,
        Instant timestamp
) {
    public static ListingReleasedEvent now(Long listingId, Long tradeId) {
        return new ListingReleasedEvent(listingId, tradeId, Instant.now());
    }
}

