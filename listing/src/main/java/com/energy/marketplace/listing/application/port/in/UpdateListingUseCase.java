package com.energy.marketplace.listing.application.port.in;

import com.energy.marketplace.listing.application.command.UpdateListingCommand;
import com.energy.marketplace.listing.application.result.ListingResult;

public interface UpdateListingUseCase {
    ListingResult update(UpdateListingCommand command);
}

