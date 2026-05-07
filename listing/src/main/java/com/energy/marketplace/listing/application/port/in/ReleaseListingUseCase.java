package com.energy.marketplace.listing.application.port.in;

import com.energy.marketplace.listing.application.command.ReleaseListingCommand;
import com.energy.marketplace.listing.application.result.ReservationResult;

public interface ReleaseListingUseCase {
    ReservationResult release(ReleaseListingCommand command);
}

