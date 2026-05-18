package com.energy.marketplace.trade.application.command.in;
import com.energy.marketplace.trade.domain.valueobject.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTradeCommand(

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
        Money amount
) {

    public CreateTradeCommand {
        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("Buyer and seller must be different users");
        }
    }
}
