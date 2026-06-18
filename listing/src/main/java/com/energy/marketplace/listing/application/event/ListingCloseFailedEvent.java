package com.energy.marketplace.listing.application.event;

import java.time.Instant;

public record ListingCloseFailedEvent(
        Long listingId,
        Long tradeId,
        String reason,
        Instant timestamp
) {
    public static ListingCloseFailedEvent now(Long listingId, Long tradeId, String reason) {
        return new ListingCloseFailedEvent(listingId, tradeId, reason, Instant.now());
    }
}
