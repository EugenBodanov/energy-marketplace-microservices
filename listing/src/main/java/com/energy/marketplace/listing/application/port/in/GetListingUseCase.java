package com.energy.marketplace.listing.application.port.in;

import com.energy.marketplace.listing.application.result.ListingResult;

public interface GetListingUseCase {
    ListingResult getById(Long listingId);
}

