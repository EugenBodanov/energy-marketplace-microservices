package com.energy.marketplace.listing.application.port.in;

import com.energy.marketplace.listing.application.command.CancelListingCommand;
import com.energy.marketplace.listing.application.result.ReservationResult;

public interface CancelListingUseCase {
    ReservationResult cancel(CancelListingCommand command);
}
