package com.energy.marketplace.listing.adapter.in.web.dto;

import java.time.Instant;

public record ListingResponse(
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

