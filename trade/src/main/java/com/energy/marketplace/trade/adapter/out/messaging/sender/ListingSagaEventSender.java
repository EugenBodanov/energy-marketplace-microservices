package com.energy.marketplace.trade.adapter.out.messaging.sender;

import com.energy.marketplace.shared.messaging.ListingSagaMessaging;
import com.energy.marketplace.trade.adapter.out.messaging.mapper.ListingMessagingMapper;
import com.energy.marketplace.trade.application.command.out.CancelListingCommand;
import com.energy.marketplace.trade.application.command.out.CloseListingCommand;
import com.energy.marketplace.trade.application.command.out.ReserveListingCommand;
import com.energy.marketplace.trade.application.port.out.SendListingCommandPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingSagaEventSender implements SendListingCommandPort {

    private final RabbitTemplate rabbitTemplate;
    private final ListingMessagingMapper mapper;
    private final JsonMapper jsonMapper;

    @Override
    public void reserveListing(ReserveListingCommand command) {
        log.info("Sending reserve listing command for trade: {}", command.tradeId());
        sendJson(
                ListingSagaMessaging.EXCHANGE,
                ListingSagaMessaging.RESERVE_LISTING_COMMAND,
                mapper.toEvent(command)
        );
    }

    @Override
    public void closeListing(CloseListingCommand command) {
        log.info("Sending close listing command for trade: {}", command.tradeId());
        sendJson(
                ListingSagaMessaging.EXCHANGE,
                ListingSagaMessaging.CLOSE_LISTING_COMMAND,
                mapper.toEvent(command)
        );
    }

    @Override
    public void cancelListing(CancelListingCommand command) {
        log.info("Sending cancel listing command for trade: {}", command.tradeId());
        sendJson(
                ListingSagaMessaging.EXCHANGE,
                ListingSagaMessaging.CANCEL_LISTING_COMMAND,
                mapper.toEvent(command)
        );
    }

    private void sendJson(String exchange, String routingKey, Object payload) {
        try {
            Message message = MessageBuilder
                    .withBody(jsonMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8))
                    .setContentType("application/json")
                    .setContentEncoding(StandardCharsets.UTF_8.name())
                    .build();
            rabbitTemplate.send(exchange, routingKey, message);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize listing saga message", exception);
        }
    }
}
