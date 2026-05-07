package com.energy.marketplace.listing.domain.exception;

public class InsufficientCapacityException extends RuntimeException {
    public InsufficientCapacityException(String message) {
        super(message);
    }

    public InsufficientCapacityException(String message, Throwable cause) {
        super(message, cause);
    }
}

