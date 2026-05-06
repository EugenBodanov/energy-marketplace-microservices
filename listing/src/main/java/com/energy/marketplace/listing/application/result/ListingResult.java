package com.energy.marketplace.listing.application.result;

import java.time.Instant;

public record ListingResult(
        Long id,
        Long sellerId,
        String title,
        String description,
        Double priceAmount,
        String priceCurrency,
        Double capacityValue,
        String capacityUnit,
        String status,
        Long reservationReference,
        Instant createdAt,
        Instant updatedAt
) {
}

