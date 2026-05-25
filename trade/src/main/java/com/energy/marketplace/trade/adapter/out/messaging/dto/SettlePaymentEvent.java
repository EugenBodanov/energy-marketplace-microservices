package com.energy.marketplace.trade.adapter.out.messaging.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SettlePaymentEvent(
        Long tradeId,
        Long paymentAuthorizationId,
        BigDecimal amount,
        String currency,
        Instant requestedAt
) {
}
