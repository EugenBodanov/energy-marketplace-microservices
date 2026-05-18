package com.energy.marketplace.trade.application.port.in;

import com.energy.marketplace.trade.application.command.in.HandleListingClosedCommand;
import com.energy.marketplace.trade.application.command.in.HandleListingReservedCommand;

public interface HandleListingSagaEventUseCase {
    void handleListingReserved(HandleListingReservedCommand command);
    void handleListingClosed(HandleListingClosedCommand command);
}
