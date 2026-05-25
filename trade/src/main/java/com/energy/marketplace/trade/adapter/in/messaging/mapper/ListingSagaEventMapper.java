package com.energy.marketplace.trade.adapter.in.messaging.mapper;

import com.energy.marketplace.trade.adapter.in.messaging.dto.*;
import com.energy.marketplace.trade.application.command.in.*;
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

    public HandleListingReservationFailedCommand toCommand(ListingReservationFailedEventMessage message) {
        return new HandleListingReservationFailedCommand(message.tradeId(), message.listingId(), message.occurredAt());
    }

    public HandleListingCloseFailedCommand toCommand(ListingCloseFailedEventMessage message) {
        return new HandleListingCloseFailedCommand(message.tradeId(), message.listingId(), message.occurredAt());
    }

    public HandleCancelListingSuccess toCommand(ListingCompensationSucceededEventMessage message) {
        return new HandleCancelListingSuccess(message.tradeId(), message.listingId(), message.occurredAt());
    }

    public HandleCancelListingFailed toCommand(ListingCompensationFailedEventMessage message) {
        return new HandleCancelListingFailed(message.tradeId(), message.listingId(), message.occurredAt());
    }
}
