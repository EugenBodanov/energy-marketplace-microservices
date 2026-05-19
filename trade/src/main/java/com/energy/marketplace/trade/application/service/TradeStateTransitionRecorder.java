package com.energy.marketplace.trade.application.service;

import com.energy.marketplace.trade.application.port.out.SaveTradeStateHistoryPort;
import com.energy.marketplace.trade.domain.model.Trade;
import com.energy.marketplace.trade.domain.model.TradeStateHistory;
import com.energy.marketplace.trade.domain.model.TradeStatus;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReason;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReasonCode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeStateTransitionRecorder {

    private final SaveTradeStateHistoryPort saveTradeStateHistoryPort;

    public void transition(
            @NotNull(message = "Trade cannot be null")
            Trade trade,
            @NotNull(message = "Reason code cannot be null")
            TradeStateTransitionReasonCode reasonCode,
            @NotNull(message = "Status change cannot be null")
            Runnable statusChange
    ) {

        TradeStatus fromStatus = trade.getStatus();

        statusChange.run();

        TradeStateHistory history = new TradeStateHistory(
                trade.getId(),
                fromStatus,
                trade.getStatus(),
                TradeStateTransitionReason.of(reasonCode)
        );

        saveTradeStateHistoryPort.save(history);
    }
}
