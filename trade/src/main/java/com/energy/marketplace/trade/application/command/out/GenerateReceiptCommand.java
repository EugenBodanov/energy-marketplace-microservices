package com.energy.marketplace.trade.application.command.out;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import com.energy.marketplace.trade.domain.valueobject.Money;

public record GenerateReceiptCommand(

        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,

        @NotNull(message = "Buyer id must not be null")
        @Positive(message = "Buyer id must be positive")
        Long buyerId,

        @NotNull(message = "Seller id must not be null")
        @Positive(message = "Seller id must be positive")
        Long sellerId,

        @NotNull(message = "Listing id must not be null")
        @Positive(message = "Listing id must be positive")
        Long listingId,

        @NotNull(message = "Amount must not be null")
        @Positive(message = "Amount must be positive")
        Money amount,

        @NotNull
        Instant tradeCompletedAt

) {

    public GenerateReceiptCommand {
        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("Buyer and seller must be different users");
        }
    }

    public static GenerateReceiptCommand of(
            Long tradeId,
            Long buyerId,
            Long sellerId,
            Long listingId,
            Money amount
    ) {
        return new GenerateReceiptCommand(
                tradeId,
                buyerId,
                sellerId,
                listingId,
                amount,
                Instant.now()
        );
    }

}