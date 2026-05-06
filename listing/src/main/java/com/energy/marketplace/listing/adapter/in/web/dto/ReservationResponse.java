package com.energy.marketplace.listing.adapter.in.web.dto;

public record ReservationResponse(
        Long listingId,
        String previousStatus,
        String newStatus,
        Long reservationReference,
        boolean success,
        String errorMessage
) {
}

