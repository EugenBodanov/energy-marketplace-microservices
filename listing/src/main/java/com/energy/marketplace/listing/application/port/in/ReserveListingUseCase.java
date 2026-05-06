package com.energy.marketplace.listing.application.port.in;

import com.energy.marketplace.listing.application.command.ReserveListingCommand;
import com.energy.marketplace.listing.application.result.ReservationResult;

public interface ReserveListingUseCase {
    ReservationResult reserve(ReserveListingCommand command);
}

