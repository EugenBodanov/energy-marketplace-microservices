package com.energy.marketplace.listing.application.event;

import java.time.Instant;

public record ListingCreatedEvent(
        Long listingId,
        Long sellerId,
        String title,
        Double priceAmount,
        String priceCurrency,
        Double capacityValue,
        String capacityUnit,
        Instant timestamp
) {
    public static ListingCreatedEvent now(Long listingId, Long sellerId, String title, Double priceAmount,
                                          String priceCurrency, Double capacityValue, String capacityUnit) {
        return new ListingCreatedEvent(listingId, sellerId, title, priceAmount, priceCurrency,
                capacityValue, capacityUnit, Instant.now());
    }
}

