package com.energy.marketplace.listing.domain.valueObject;

import lombok.Getter;

@Getter
public class Capacity {
    private final Double value;
    private final String unit;

    public Capacity(Double value, String unit) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Capacity value must be greater than 0");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Capacity unit must not be blank");
        }
        this.value = value;
        this.unit = unit;
    }

    public static Capacity ofKWh(Double value) {
        return new Capacity(value, "kWh");
    }
}

