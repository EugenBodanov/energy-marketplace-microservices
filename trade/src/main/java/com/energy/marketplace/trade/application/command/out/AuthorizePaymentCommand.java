package com.energy.marketplace.trade.application.command.out;

import com.energy.marketplace.trade.domain.valueobject.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record AuthorizePaymentCommand(

        @NotNull(message = "Trade id must not be null")
        @Positive(message = "Trade id must be positive")
        Long tradeId,

        @NotNull(message = "Buyer id must not be null")
        @Positive(message = "Buyer id must be positive")
        Long buyerId,

        @NotNull(message = "Seller id must not be null")
        @Positive(message = "Seller id must be positive")
        Long sellerId,

        @NotNull(message = "Amount must not be null")
        @Positive(message = "Expected amount must be positive")
        Money amount,

        @NotNull(message = "Requested at must not be null")
        Instant requestedAt
) {

    public AuthorizePaymentCommand {
        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("Buyer and seller must be different users");
        }
    }

    public static AuthorizePaymentCommand of(
            Long tradeId,
            Long buyerId,
            Long sellerId,
            Money amount
    ) {
        return new AuthorizePaymentCommand(
                tradeId,
                buyerId,
                sellerId,
                amount,
                Instant.now()
        );
    }
}