package com.energy.marketplace.trade.adapter.in.messaging.mapper;

import com.energy.marketplace.trade.adapter.in.messaging.dto.ListingClosedEventMessage;
import com.energy.marketplace.trade.adapter.in.messaging.dto.ListingReservedEventMessage;
import com.energy.marketplace.trade.application.command.in.HandleListingClosedCommand;
import com.energy.marketplace.trade.application.command.in.HandleListingReservedCommand;
import org.springframework.stereotype.Component;

@Component
public class ListingSagaEventMapper {

    public HandleListingReservedCommand toCommand(ListingReservedEventMessage message) {
        return new HandleListingReservedCommand(message.tradeId(), message.listingId(),
                message.reservationId(), message.occurredAt());
    }

    public HandleListingClosedCommand toCommand(ListingClosedEventMessage message) {
        return new HandleListingClosedCommand(message.tradeId(), message.listingId(), message.occurredAt());
    }

}
