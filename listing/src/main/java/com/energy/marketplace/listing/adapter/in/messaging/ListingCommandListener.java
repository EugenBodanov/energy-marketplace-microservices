package com.energy.marketplace.listing.adapter.in.messaging;

import com.energy.marketplace.listing.application.command.CloseListingCommand;
import com.energy.marketplace.listing.application.command.ReleaseListingCommand;
import com.energy.marketplace.listing.application.command.ReserveListingCommand;
import com.energy.marketplace.listing.application.port.in.CloseListingUseCase;
import com.energy.marketplace.listing.application.port.in.ReleaseListingUseCase;
import com.energy.marketplace.listing.application.port.in.ReserveListingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListingCommandListener {

    private final ReserveListingUseCase reserveListingUseCase;
    private final ReleaseListingUseCase releaseListingUseCase;
    private final CloseListingUseCase closeListingUseCase;

    @RabbitListener(queues = "listing.commands.queue")
    public void handleListingCommand(String message) {
        // This will be expanded to handle different command types
        // For now, just log
        log.debug("Received message on listing.commands.queue: {}", message);
    }

    public void handleReserveCommand(ReserveListingCommand command) {
        log.info("Handling ReserveListingCommand for listing {} and trade {}",
                command.listingId(), command.tradeId());
        try {
            reserveListingUseCase.reserve(command);
        } catch (Exception e) {
            log.error("Error processing ReserveListingCommand", e);
        }
    }

    public void handleReleaseCommand(ReleaseListingCommand command) {
        log.info("Handling ReleaseListingCommand for listing {} and trade {}",
                command.listingId(), command.tradeId());
        try {
            releaseListingUseCase.release(command);
        } catch (Exception e) {
            log.error("Error processing ReleaseListingCommand", e);
        }
    }

    public void handleCloseCommand(CloseListingCommand command) {
        log.info("Handling CloseListingCommand for listing {} and trade {}",
                command.listingId(), command.tradeId());
        try {
            closeListingUseCase.close(command);
        } catch (Exception e) {
            log.error("Error processing CloseListingCommand", e);
        }
    }
}

