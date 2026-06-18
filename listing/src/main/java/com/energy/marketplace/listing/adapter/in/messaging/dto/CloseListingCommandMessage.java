package com.energy.marketplace.listing.adapter.in.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record CloseListingCommandMessage(
        @JsonProperty("eventType")
        @NotNull(message = "Event type must not be null")
        String eventType,

        @JsonProperty("tradeId")
        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,

        @JsonProperty("listingId")
        @NotNull(message = "Listing id must not be null")
        @Positive(message = "Listing id must be positive")
        Long listingId,

        @JsonProperty("occurredAt")
        @NotNull(message = "Occurred at must not be null")
        Instant occurredAt
) {
}
