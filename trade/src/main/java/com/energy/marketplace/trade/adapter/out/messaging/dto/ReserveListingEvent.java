package com.energy.marketplace.trade.adapter.out.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record ReserveListingEvent(
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

        @JsonProperty("buyerId")
        @NotNull(message = "Buyer id must not be null")
        @Positive(message = "Buyer id must be positive")
        Long buyerId,

        @JsonProperty("sellerId")
        @NotNull(message = "Seller id must not be null")
        @Positive(message = "Seller id must be positive")
        Long sellerId,

        @JsonProperty("amount")
        Long amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("occurredAt")
        @NotNull(message = "Occurred at must not be null")
        Instant occurredAt
) {
}
