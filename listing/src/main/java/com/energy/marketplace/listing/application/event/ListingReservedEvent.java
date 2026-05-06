package com.energy.marketplace.listing.application.event;

import java.time.Instant;

public record ListingReservedEvent(
        Long listingId,
        Long tradeId,
        Instant timestamp
) {
    public static ListingReservedEvent now(Long listingId, Long tradeId) {
        return new ListingReservedEvent(listingId, tradeId, Instant.now());
    }
}

