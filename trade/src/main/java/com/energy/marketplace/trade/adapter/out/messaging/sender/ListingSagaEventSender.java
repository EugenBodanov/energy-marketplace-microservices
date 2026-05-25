package com.energy.marketplace.trade.adapter.out.messaging.sender;

import com.energy.marketplace.trade.adapter.out.messaging.mapper.ListingMessagingMapper;
import com.energy.marketplace.trade.application.command.out.CancelListingCommand;
import com.energy.marketplace.trade.application.command.out.CloseListingCommand;
import com.energy.marketplace.trade.application.command.out.ReserveListingCommand;
import com.energy.marketplace.trade.application.port.out.SendListingCommandPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingSagaEventSender implements SendListingCommandPort {

    private final RabbitTemplate rabbitTemplate;
    private final ListingMessagingMapper mapper;

    private static final String EXCHANGE = "trade.saga.exchange";
    private static final String RESERVE_ROUTING_KEY = "listing.reserve.command";
    private static final String CLOSE_ROUTING_KEY = "listing.close.command";
    private static final String CANCEL_ROUTING_KEY = "listing.cancel.command";

    @Override
    public void reserveListing(ReserveListingCommand command) {
        log.info("Sending reserve listing command for trade: {}", command.tradeId());
        rabbitTemplate.convertAndSend(EXCHANGE, RESERVE_ROUTING_KEY, mapper.toEvent(command));
    }

    @Override
    public void closeListing(CloseListingCommand command) {
        log.info("Sending close listing command for trade: {}", command.tradeId());
        rabbitTemplate.convertAndSend(EXCHANGE, CLOSE_ROUTING_KEY, mapper.toEvent(command));
    }

    @Override
    public void cancelListing(CancelListingCommand command) {
        log.info("Sending cancel listing command for trade: {}", command.tradeId());
        rabbitTemplate.convertAndSend(EXCHANGE, CANCEL_ROUTING_KEY, mapper.toEvent(command));
    }
}
