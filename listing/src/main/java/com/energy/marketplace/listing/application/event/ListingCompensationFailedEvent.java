package com.energy.marketplace.listing.application.event;

import java.time.Instant;

public record ListingCompensationFailedEvent(
        Long listingId,
        Long tradeId,
        String reason,
        Instant timestamp
) {
    public static ListingCompensationFailedEvent now(Long listingId, Long tradeId, String reason) {
        return new ListingCompensationFailedEvent(listingId, tradeId, reason, Instant.now());
    }
}
