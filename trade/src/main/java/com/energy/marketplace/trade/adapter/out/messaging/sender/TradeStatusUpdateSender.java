package com.energy.marketplace.trade.adapter.out.messaging.sender;

import com.energy.marketplace.trade.application.event.TradeStatusUpdate;
import com.energy.marketplace.trade.application.port.out.PublishTradeUpdatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeStatusUpdateSender implements PublishTradeUpdatePort {

    private static final String EXCHANGE = "trade.saga.exchange";
    private static final String TRADE_STATUS_UPDATE_ROUTING_KEY = "trade.status_update.event";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishTradeStateUpdate(TradeStatusUpdate tradeStatusUpdate) {
        log.info(
                "Publishing trade state update for trade: {}, status: {}",
                tradeStatusUpdate.tradeId(),
                tradeStatusUpdate.status()
        );
        rabbitTemplate.convertAndSend(EXCHANGE, TRADE_STATUS_UPDATE_ROUTING_KEY, tradeStatusUpdate);
    }
}
