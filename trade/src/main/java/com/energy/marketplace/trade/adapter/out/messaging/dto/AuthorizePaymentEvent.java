package com.energy.marketplace.trade.adapter.out.messaging.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AuthorizePaymentEvent(
        Long tradeId,
        Long buyerId,
        Long sellerId,
        BigDecimal amount,
        String currency,
        Instant requestedAt
) {
}
