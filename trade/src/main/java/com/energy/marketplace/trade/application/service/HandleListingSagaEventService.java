package com.energy.marketplace.trade.application.service;

import com.energy.marketplace.trade.application.command.in.HandleListingClosedCommand;
import com.energy.marketplace.trade.application.command.in.HandleListingReservedCommand;
import com.energy.marketplace.trade.application.command.out.AuthorizePaymentCommand;
import com.energy.marketplace.trade.application.command.out.SettlePaymentCommand;
import com.energy.marketplace.trade.application.port.in.HandleListingSagaEventUseCase;
import com.energy.marketplace.trade.application.port.out.*;
import com.energy.marketplace.trade.domain.exception.InvalidTradeStateException;
import com.energy.marketplace.trade.application.exception.TradeSagaProcessingException;
import com.energy.marketplace.trade.domain.model.Trade;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReasonCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@lombok.RequiredArgsConstructor
public class HandleListingSagaEventService implements HandleListingSagaEventUseCase {

    private final SendBillingCommandPort sendBillingCommandPort;
    private final LoadTradePort loadTradePort;
    private final SaveTradePort saveTradePort;
    private final TradeStateTransitionRecorder transitionRecorder;
    private final TradeStateUpdateNotifier tradeStateUpdateNotifier;

    @Override
    @Transactional
    public void handleListingReserved(HandleListingReservedCommand command) {
        try {
            Trade trade = loadTradePort.load(command.tradeId());

            validateListingBelongsToTrade(trade, command.listingId());

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.LISTING_RESERVED,
                    trade::markListingReserved
            );

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.PAYMENT_AUTHORIZATION_REQUESTED,
                    trade::markPaymentAuthorizationPending
            );

            saveTradePort.save(trade);

            sendBillingCommandPort.authorizePayment(
                    AuthorizePaymentCommand.of(
                            trade.getId(),
                            trade.getBuyerId(),
                            trade.getSellerId(),
                            trade.getAmount()
                    )
            );

            tradeStateUpdateNotifier.publishTradeStateUpdate(trade, TradeStateTransitionReasonCode.PAYMENT_AUTHORIZATION_REQUESTED);

        } catch (InvalidTradeStateException exception) {
            throw new TradeSagaProcessingException(
                    "Cannot handle ListingReserved event because trade state transition is invalid",
                    exception
            );
        } catch (RuntimeException exception) {
            throw new TradeSagaProcessingException(
                    "Failed to handle ListingReserved event for tradeId=" + command.tradeId(),
                    exception
            );
        }
    }

    @Override
    @Transactional
    public void handleListingClosed(HandleListingClosedCommand command) {
        try {
            Trade trade = loadTradePort.load(command.tradeId());

            validateListingBelongsToTrade(trade, command.listingId());

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.LISTING_CLOSED,
                    trade::markListingClosed
            );

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.PAYMENT_SETTLEMENT_REQUESTED,
                    trade::markPaymentSettlementPending
            );

            saveTradePort.save(trade);

            sendBillingCommandPort.settlePayment(
                    SettlePaymentCommand.of(
                            trade.getId(),
                            trade.getPaymentAuthorizationId(),
                            trade.getAmount()
                    )
            );

            tradeStateUpdateNotifier.publishTradeStateUpdate(trade, TradeStateTransitionReasonCode.PAYMENT_SETTLEMENT_REQUESTED);

        } catch (InvalidTradeStateException exception) {
            throw new TradeSagaProcessingException(
                    "Cannot handle ListingClosed event because trade state transition is invalid",
                    exception
            );
        } catch (RuntimeException exception) {
            throw new TradeSagaProcessingException(
                    "Failed to handle ListingClosed event for tradeId=" + command.tradeId(),
                    exception
            );
        }
    }

    private void validateListingBelongsToTrade(Trade trade, Long listingId) {
        if (!trade.getListingId().equals(listingId)) {
            throw new TradeSagaProcessingException(
                    "Listing id mismatch for tradeId=" + trade.getId()
                            + ". Expected listingId=" + trade.getListingId()
                            + ", actual listingId=" + listingId
            );
        }
    }
}
