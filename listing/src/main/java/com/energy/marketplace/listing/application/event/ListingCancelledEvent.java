package com.energy.marketplace.listing.application.event;

import java.time.Instant;

public record ListingCancelledEvent(
        Long listingId,
        Long tradeId,
        Instant timestamp
) {
    public static ListingCancelledEvent now(Long listingId, Long tradeId) {
        return new ListingCancelledEvent(listingId, tradeId, Instant.now());
    }
}
