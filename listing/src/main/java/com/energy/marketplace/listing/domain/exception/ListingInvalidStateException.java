package com.energy.marketplace.listing.domain.exception;

public class ListingInvalidStateException extends RuntimeException {
    public ListingInvalidStateException(String message) {
        super(message);
    }

    public ListingInvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }
}

