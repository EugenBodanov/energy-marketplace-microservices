package com.energy.marketplace.listing.adapter.in.messaging.mapper;

import com.energy.marketplace.listing.adapter.in.messaging.dto.CancelListingCommandMessage;
import com.energy.marketplace.listing.adapter.in.messaging.dto.CloseListingCommandMessage;
import com.energy.marketplace.listing.adapter.in.messaging.dto.ReserveListingCommandMessage;
import com.energy.marketplace.listing.application.command.CloseListingCommand;
import com.energy.marketplace.listing.application.command.CancelListingCommand;
import com.energy.marketplace.listing.application.command.ReserveListingCommand;
import org.springframework.stereotype.Component;

@Component
public class ListingCommandMapper {

    public ReserveListingCommand toCommand(ReserveListingCommandMessage message) {
        return new ReserveListingCommand(message.listingId(), message.tradeId());
    }

    public CloseListingCommand toCommand(CloseListingCommandMessage message) {
        return new CloseListingCommand(message.listingId(), message.tradeId());
    }

    public CancelListingCommand toCommand(CancelListingCommandMessage message) {
        return new CancelListingCommand(message.listingId(), message.tradeId());
    }
}

