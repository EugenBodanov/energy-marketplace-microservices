package com.energy.marketplace.listing.domain.exception;

public class ListingNotFoundException extends RuntimeException {
    public ListingNotFoundException(String message) {
        super(message);
    }

    public ListingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

