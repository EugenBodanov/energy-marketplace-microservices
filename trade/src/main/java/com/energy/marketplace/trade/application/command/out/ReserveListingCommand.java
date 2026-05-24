package com.energy.marketplace.trade.application.command.out;

import com.energy.marketplace.trade.domain.valueObject.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record ReserveListingCommand(

        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,

        @NotNull(message = "Listing id must not be null")
        @Positive(message = "Listing id must be positive")
        Long listingId,

        @NotNull(message = "Buyer id must not be null")
        @Positive(message = "Buyer id must be positive")
        Long buyerId,

        @NotNull(message = "Seller id must not be null")
        @Positive(message = "Seller id must be positive")
        Long sellerId,

        @NotNull(message = "Expected amount must not be null")
        @Positive(message = "Expected amount must be positive")
        Money expectedAmount,

        @NotNull(message = "Requested at must not be null")
        Instant requestedAt
) {

    public ReserveListingCommand {
        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("Buyer and seller must be different users");
        }
    }

    public static ReserveListingCommand of(
            Long tradeId,
            Long listingId,
            Long buyerId,
            Long sellerId,
            Money expectedAmount
    ) {
        return new ReserveListingCommand(
                tradeId,
                listingId,
                buyerId,
                sellerId,
                expectedAmount,
                Instant.now()
        );
    }
}