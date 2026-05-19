package com.energy.marketplace.trade.application.service;

import com.energy.marketplace.trade.application.event.TradeStatusUpdate;
import com.energy.marketplace.trade.application.port.out.PublishTradeUpdatePort;
import com.energy.marketplace.trade.domain.model.Trade;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReasonCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeStateUpdateNotifier {

    private final PublishTradeUpdatePort publishTradeUpdatePort;

    public void publishTradeStateUpdate(
            @Valid @NotNull(message = "Trade must not be null") Trade trade,
            @NotNull(message = "Trade state transition reason code must not be null") TradeStateTransitionReasonCode reasonCode
    ) {

        TradeStatusUpdate update = TradeStatusUpdate.of(
                trade.getId(),
                trade.getStatus(),
                reasonCode
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            publishSafely(update);
                        }
                    }
            );
        } else {
            publishSafely(update);
        }
    }

    private void publishSafely(TradeStatusUpdate update) {
        try {
            publishTradeUpdatePort.publishTradeStateUpdate(update);
        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to publish trade state update. tradeId={}, status={}, reasonCode={}",
                    update.tradeId(),
                    update.status(),
                    update.reasonCode(),
                    exception
            );
        }
    }
}
