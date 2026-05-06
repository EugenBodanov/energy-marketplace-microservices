package com.energy.marketplace.listing.application.command;

public record ReserveListingCommand(
        Long listingId,
        Long tradeId
) {
}

