package com.energy.marketplace.listing.application.port.in;

import com.energy.marketplace.listing.application.command.CreateListingCommand;
import com.energy.marketplace.listing.application.result.ListingResult;

public interface CreateListingUseCase {
    ListingResult create(CreateListingCommand command);
}

