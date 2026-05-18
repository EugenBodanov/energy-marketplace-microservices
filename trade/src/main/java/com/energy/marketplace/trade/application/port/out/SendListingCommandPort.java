package com.energy.marketplace.trade.application.port.out;

import com.energy.marketplace.trade.application.command.out.CloseListingCommand;
import com.energy.marketplace.trade.application.command.out.ReserveListingCommand;

public interface SendListingCommandPort {

    void reserveListing(ReserveListingCommand command);

    void closeListing(CloseListingCommand command);

}