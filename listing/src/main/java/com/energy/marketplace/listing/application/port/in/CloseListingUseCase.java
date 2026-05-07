package com.energy.marketplace.listing.application.port.in;

import com.energy.marketplace.listing.application.command.CloseListingCommand;
import com.energy.marketplace.listing.application.result.ReservationResult;

public interface CloseListingUseCase {
    ReservationResult close(CloseListingCommand command);
}

