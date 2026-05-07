package com.energy.marketplace.listing.application.port.in;

import com.energy.marketplace.listing.application.command.DeleteListingCommand;

public interface DeleteListingUseCase {
    void delete(DeleteListingCommand command);
}

