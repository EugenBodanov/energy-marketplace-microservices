package com.energy.marketplace.listing.application.command;

public record CancelListingCommand(
        Long listingId,
        Long tradeId
) {
}
