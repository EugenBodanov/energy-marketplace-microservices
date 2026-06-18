package com.energy.marketplace.listing.adapter.in.messaging;

import com.energy.marketplace.shared.messaging.ListingSagaMessaging;
import com.energy.marketplace.listing.adapter.in.messaging.dto.CancelListingCommandMessage;
import com.energy.marketplace.listing.adapter.in.messaging.dto.CloseListingCommandMessage;
import com.energy.marketplace.listing.adapter.in.messaging.dto.ReserveListingCommandMessage;
import com.energy.marketplace.listing.adapter.in.messaging.mapper.ListingCommandMapper;
import com.energy.marketplace.listing.application.command.CloseListingCommand;
import com.energy.marketplace.listing.application.command.ReleaseListingCommand;
import com.energy.marketplace.listing.application.command.ReserveListingCommand;
import com.energy.marketplace.listing.application.command.CancelListingCommand;
import com.energy.marketplace.listing.application.port.in.CloseListingUseCase;
import com.energy.marketplace.listing.application.port.in.ReleaseListingUseCase;
import com.energy.marketplace.listing.application.port.in.ReserveListingUseCase;
import com.energy.marketplace.listing.application.port.in.CancelListingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListingCommandListener {

    private final ReserveListingUseCase reserveListingUseCase;
    private final ReleaseListingUseCase releaseListingUseCase;
    private final CloseListingUseCase closeListingUseCase;
    private final CancelListingUseCase cancelListingUseCase;
    private final JsonMapper jsonMapper;
    private final ListingCommandMapper commandMapper;

    @RabbitListener(queues = "${listing.commands.queue:" + ListingSagaMessaging.LISTING_COMMANDS_QUEUE + "}")
    public void handleListingCommand(Message amqpMessage) {
        String message = "";
        try {
            // Convert the raw byte array body directly to a UTF-8 String manually
            message = new String(amqpMessage.getBody(), StandardCharsets.UTF_8);
            log.error("Received listing command message: {}", message);

            JsonNode commandNode = jsonMapper.readTree(message);
            String eventType = readCommandType(commandNode);
            switch (eventType) {
                case "LISTING_RESERVE" -> {
                    ReserveListingCommandMessage cmd = jsonMapper.readValue(message, ReserveListingCommandMessage.class);
                    handleReserveCommand(commandMapper.toCommand(cmd));
                }
                case "LISTING_CLOSE" -> {
                    CloseListingCommandMessage cmd = jsonMapper.readValue(message, CloseListingCommandMessage.class);
                    handleCloseCommand(commandMapper.toCommand(cmd));
                }
                case "LISTING_CANCEL" -> {
                    CancelListingCommandMessage cmd = jsonMapper.readValue(message, CancelListingCommandMessage.class);
                    handleCancelCommand(commandMapper.toCommand(cmd));
                }
                default -> throw new IllegalArgumentException(
                        "Unknown listing command type: " + eventType
                );
            }
        } catch (JacksonException | IllegalArgumentException e) {
            log.error("Failed to handle listing command: {}", message, e);
        }
    }

    private String readCommandType(JsonNode root) {
        String commandType = root.path("eventType").asText(null);

        if (commandType == null || commandType.isBlank()) {
            throw new IllegalArgumentException("Missing eventType in listing command");
        }

        return commandType;
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

    public void handleCancelCommand(CancelListingCommand command) {
        log.info("Handling CancelListingCommand (compensation) for listing {} and trade {}",
                command.listingId(), command.tradeId());
        try {
            cancelListingUseCase.cancel(command);
        } catch (Exception e) {
            log.error("Error processing CancelListingCommand", e);
        }
    }
}