package com.energy.marketplace.trade.adapter.out.messaging.dto;

import java.time.Instant;

public record CloseListingEvent(
        Long tradeId,
        Long listingId,
        Instant requestedAt
) {
}
