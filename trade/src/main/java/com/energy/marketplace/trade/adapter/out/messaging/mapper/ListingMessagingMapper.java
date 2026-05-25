package com.energy.marketplace.trade.adapter.out.messaging.mapper;

import com.energy.marketplace.trade.adapter.out.messaging.dto.CancelListingEvent;
import com.energy.marketplace.trade.adapter.out.messaging.dto.CloseListingEvent;
import com.energy.marketplace.trade.adapter.out.messaging.dto.ReserveListingEvent;
import com.energy.marketplace.trade.application.command.out.CancelListingCommand;
import com.energy.marketplace.trade.application.command.out.CloseListingCommand;
import com.energy.marketplace.trade.application.command.out.ReserveListingCommand;
import org.springframework.stereotype.Component;

@Component
public class ListingMessagingMapper {

    public ReserveListingEvent toEvent(ReserveListingCommand command) {
        return new ReserveListingEvent(
                command.tradeId(),
                command.listingId(),
                command.buyerId(),
                command.sellerId(),
                command.expectedAmount().amount(),
                command.expectedAmount().currencyCode(),
                command.requestedAt()
        );
    }

    public CloseListingEvent toEvent(CloseListingCommand command) {
        return new CloseListingEvent(
                command.tradeId(),
                command.listingId(),
                command.requestedAt()
        );
    }

    public CancelListingEvent toEvent(CancelListingCommand command) {
        return new CancelListingEvent(
                command.tradeId(),
                command.listingId(),
                command.requestedAt()
        );
    }
}
