package com.energy.marketplace.listing.application.port.out;

import com.energy.marketplace.listing.domain.model.Listing;

public interface SaveListingPort {
    Listing save(Listing listing);
}

