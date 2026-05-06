package com.energy.marketplace.listing.application.command;

public record CloseListingCommand(
        Long listingId,
        Long tradeId
) {
}

