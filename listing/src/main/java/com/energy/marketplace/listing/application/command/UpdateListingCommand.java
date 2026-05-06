package com.energy.marketplace.listing.application.command;

import com.energy.marketplace.listing.domain.valueObject.Capacity;
import com.energy.marketplace.listing.domain.valueObject.ListingPrice;

public record UpdateListingCommand(
        Long listingId,
        String title,
        String description,
        Double priceAmount,
        String priceCurrency,
        Double capacityValue,
        String capacityUnit
) {
    public ListingPrice getPrice() {
        return new ListingPrice(priceAmount, priceCurrency);
    }

    public Capacity getCapacity() {
        return new Capacity(capacityValue, capacityUnit);
    }
}

