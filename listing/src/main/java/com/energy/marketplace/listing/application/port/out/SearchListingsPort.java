package com.energy.marketplace.listing.application.port.out;

import com.energy.marketplace.listing.domain.model.Listing;
import com.energy.marketplace.listing.domain.valueObject.ListingStatus;

import java.util.List;

public interface SearchListingsPort {
    List<Listing> findAll();
    List<Listing> findByStatus(ListingStatus status);
    List<Listing> findBySellerId(Long sellerId);
}

