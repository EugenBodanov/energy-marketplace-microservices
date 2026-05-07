package com.energy.marketplace.listing.application.command;

public record ReleaseListingCommand(
        Long listingId,
        Long tradeId
) {
}

