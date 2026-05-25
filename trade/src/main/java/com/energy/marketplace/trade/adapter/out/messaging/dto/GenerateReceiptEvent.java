package com.energy.marketplace.trade.adapter.out.messaging.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GenerateReceiptEvent(
        Long tradeId,
        Long buyerId,
        Long sellerId,
        Long listingId,
        BigDecimal amount,
        String currency,
        Instant tradeCompletedAt
) {
}
