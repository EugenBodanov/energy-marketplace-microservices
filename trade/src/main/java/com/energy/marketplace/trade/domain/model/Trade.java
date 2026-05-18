package com.energy.marketplace.trade.domain.model;

import com.energy.marketplace.trade.domain.valueobject.Money;

import java.util.Objects;

@lombok.Getter
public class Trade {

    private Long id;
    private Long buyerId;
    private Long sellerId;
    private Long listingId;
    private Money amount;
    private TradeStatus status;

    public Trade(
            Long id,
            Long buyerId,
            Long sellerId,
            Long listingId,
            Money amount,
            TradeStatus status
    ) {
        this.id = id;
        this.buyerId = Objects.requireNonNull(buyerId, "Buyer id must not be null");
        this.sellerId = Objects.requireNonNull(sellerId, "Seller id must not be null");
        this.listingId = Objects.requireNonNull(listingId, "Listing id must not be null");
        this.amount = Objects.requireNonNull(amount, "Amount must not be null");
        this.status = Objects.requireNonNull(status, "Trade status must not be null");

        if (buyerId <= 0) {
            throw new IllegalArgumentException("Buyer id must be positive");
        }

        if (sellerId <= 0) {
            throw new IllegalArgumentException("Seller id must be positive");
        }

        if (listingId <= 0) {
            throw new IllegalArgumentException("Listing id must be positive");
        }

        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("Buyer and seller must be different users");
        }

        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Trade amount must be positive");
        }
    }

    public static Trade createTrade(
            Long buyerId,
            Long sellerId,
            Long listingId,
            Money amount
    ) {
        return new Trade(
                null,
                buyerId,
                sellerId,
                listingId,
                amount,
                TradeStatus.CREATED
        );
    }
}