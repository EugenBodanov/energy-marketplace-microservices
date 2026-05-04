package com.energy.marketplace.user.domain.valueObject;

public record Id (Long value) {

    public Id {
        if (value == null) {
            throw new IllegalArgumentException("Id must not be null");
        }

        if (value <= 0) {
            throw new IllegalArgumentException("Id must be positive");
        }
    }
}
