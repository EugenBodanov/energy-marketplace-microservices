package com.energy.marketplace.listing.domain.valueObject;

import lombok.Getter;

@Getter
public class ListingPrice {
    private final Double amount;
    private final String currency;

    public ListingPrice(Double amount, String currency) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Price amount must be greater than 0");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be blank");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public static ListingPrice ofEUR(Double amount) {
        return new ListingPrice(amount, "EUR");
    }
}

