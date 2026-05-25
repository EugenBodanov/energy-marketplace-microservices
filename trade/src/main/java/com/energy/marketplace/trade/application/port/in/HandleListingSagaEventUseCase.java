package com.energy.marketplace.trade.application.port.in;

import com.energy.marketplace.trade.application.command.in.*;

public interface HandleListingSagaEventUseCase {
    void handleListingReserved(HandleListingReservedCommand command);
    void handleListingClosed(HandleListingClosedCommand command);
    void handleListingReservationFailed(HandleListingReservationFailedCommand command);
    void cancelListingFailed(HandleCancelListingFailed command);
    void cancelListingSuccess(HandleCancelListingSuccess command);
    void listingCloseFailed(HandleListingCloseFailedCommand command);
}
