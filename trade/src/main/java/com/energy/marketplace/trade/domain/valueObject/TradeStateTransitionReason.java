package com.energy.marketplace.trade.domain.valueObject;
import java.util.Objects;

public record TradeStateTransitionReason(
        TradeStateTransitionReasonCode code,
        String details
) {

    public TradeStateTransitionReason {
        Objects.requireNonNull(code, "Transition reason code must not be null");

        if (details != null) {
            details = details.trim();
        }
    }

    public static TradeStateTransitionReason of(TradeStateTransitionReasonCode code) {
        return new TradeStateTransitionReason(code, null);
    }

    public static TradeStateTransitionReason of(
            TradeStateTransitionReasonCode code,
            String details
    ) {
        return new TradeStateTransitionReason(code, details);
    }

    public boolean hasDetails() {
        return details != null && !details.isBlank();
    }
}
