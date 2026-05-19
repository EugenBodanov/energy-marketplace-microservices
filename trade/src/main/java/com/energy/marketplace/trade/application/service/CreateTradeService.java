package com.energy.marketplace.trade.application.service;

import com.energy.marketplace.trade.application.command.in.CreateTradeCommand;
import com.energy.marketplace.trade.application.command.out.ReserveListingCommand;
import com.energy.marketplace.trade.application.event.TradeStatusUpdate;
import com.energy.marketplace.trade.application.port.out.PublishTradeUpdatePort;
import com.energy.marketplace.trade.application.port.out.ValidateTradeParticipantsPort;
import com.energy.marketplace.trade.application.exception.TradeSagaProcessingException;
import com.energy.marketplace.trade.application.port.in.CreateTradeUseCase;
import com.energy.marketplace.trade.application.port.out.SaveTradePort;
import com.energy.marketplace.trade.application.port.out.SendListingCommandPort;
import com.energy.marketplace.trade.application.result.CreateTradeResult;
import com.energy.marketplace.trade.domain.exception.InvalidTradeStateException;
import com.energy.marketplace.trade.domain.model.Trade;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReasonCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@lombok.RequiredArgsConstructor
public class CreateTradeService implements CreateTradeUseCase {

    private final SaveTradePort saveTradePort;
    private final ValidateTradeParticipantsPort validateTradeParticipantsPort;
    private final SendListingCommandPort sendListingCommandPort;
    private final TradeStateTransitionRecorder transitionRecorder;

    @Override
    @Transactional
    public CreateTradeResult createTrade(CreateTradeCommand command) {
        try {
            Trade trade = Trade.createTrade(
                    command.buyerId(),
                    command.sellerId(),
                    command.listingId(),
                    command.amount()
            );

            trade = saveTradePort.save(trade);

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.USER_VALIDATION_REQUESTED,
                    trade::markUserValidationPending
            );

            trade = saveTradePort.save(trade);

            boolean buyerValid = validateTradeParticipantsPort.validateBuyer(trade.getBuyerId());
            boolean sellerValid = validateTradeParticipantsPort.validateSeller(trade.getSellerId());

            if (!buyerValid || !sellerValid) {
                transitionRecorder.transition(
                        trade,
                        TradeStateTransitionReasonCode.USER_VALIDATION_FAILED,
                        trade::markFailedFromUserValidation
                );

                trade = saveTradePort.save(trade);

                return new CreateTradeResult(
                        trade.getId(),
                        trade.getStatus()
                );
            }

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.USER_VALIDATION_SUCCEEDED,
                    trade::markUserValidated
            );

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.LISTING_RESERVATION_REQUESTED,
                    trade::markListingReservationPending
            );

            trade = saveTradePort.save(trade);

            sendListingCommandPort.reserveListing(
                    ReserveListingCommand.of(
                            trade.getId(),
                            trade.getListingId(),
                            trade.getBuyerId(),
                            trade.getSellerId(),
                            trade.getAmount()
                    )
            );

            return new CreateTradeResult(
                    trade.getId(),
                    trade.getStatus()
            );

        } catch (TradeSagaProcessingException exception) {
            throw exception;
        } catch (InvalidTradeStateException exception) {
            throw new TradeSagaProcessingException(
                    "Cannot create trade because trade state transition is invalid",
                    exception
            );
        } catch (RuntimeException exception) {
            throw new TradeSagaProcessingException(
                    "Failed to create trade",
                    exception
            );
        }
    }
}
