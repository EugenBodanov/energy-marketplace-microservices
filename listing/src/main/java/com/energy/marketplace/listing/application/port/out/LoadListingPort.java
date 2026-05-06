package com.energy.marketplace.listing.application.port.out;

import com.energy.marketplace.listing.domain.model.Listing;

import java.util.Optional;

public interface LoadListingPort {
    Optional<Listing> findById(Long listingId);
}

