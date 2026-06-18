package com.energy.marketplace.listing.adapter.out.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record ListingCloseFailedEventMessage(
        @JsonProperty("eventType")
        String eventType,

        @JsonProperty("listingId")
        Long listingId,

        @JsonProperty("tradeId")
        Long tradeId,

        @JsonProperty("reason")
        String reason,

        @JsonProperty("occurredAt")
        Instant occurredAt
) {
}
