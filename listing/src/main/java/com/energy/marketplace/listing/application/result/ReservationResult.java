package com.energy.marketplace.listing.application.result;

public record ReservationResult(
        Long listingId,
        String previousStatus,
        String newStatus,
        Long reservationReference,
        boolean success,
        String errorMessage
) {
    public static ReservationResult success(Long listingId, String previousStatus, String newStatus, Long reservationReference) {
        return new ReservationResult(listingId, previousStatus, newStatus, reservationReference, true, null);
    }

    public static ReservationResult failure(Long listingId, String errorMessage) {
        return new ReservationResult(listingId, null, null, null, false, errorMessage);
    }
}

