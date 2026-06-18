package com.energy.marketplace.trade.adapter.out.messaging.sender;

import com.energy.marketplace.trade.application.event.TradeStatusUpdate;
import com.energy.marketplace.trade.application.port.out.PublishTradeUpdatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeStatusUpdateSender implements PublishTradeUpdatePort {

    private static final String EXCHANGE = "trade.saga.exchange";
    private static final String TRADE_STATUS_UPDATE_ROUTING_KEY = "trade.status_update.event";

    private final RabbitTemplate rabbitTemplate;
    private final JsonMapper jsonMapper;

    @Override
    public void publishTradeStateUpdate(TradeStatusUpdate tradeStatusUpdate) {
        log.info(
                "Publishing trade state update for trade: {}, status: {}",
                tradeStatusUpdate.tradeId(),
                tradeStatusUpdate.status()
        );
        try {
            Message message = MessageBuilder
                    .withBody(jsonMapper.writeValueAsString(tradeStatusUpdate).getBytes(StandardCharsets.UTF_8))
                    .setContentType("application/json")
                    .setContentEncoding(StandardCharsets.UTF_8.name())
                    .build();
            rabbitTemplate.send(EXCHANGE, TRADE_STATUS_UPDATE_ROUTING_KEY, message);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize trade status update", exception);
        }
    }
}
