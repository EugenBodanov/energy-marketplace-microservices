package com.energy.marketplace.trade.application.service;

import com.energy.marketplace.trade.application.command.in.HandlePaymentAuthorizedCommand;
import com.energy.marketplace.trade.application.command.in.HandlePaymentSettledCommand;
import com.energy.marketplace.trade.application.command.in.HandleReceiptGeneratedCommand;
import com.energy.marketplace.trade.application.command.out.CloseListingCommand;
import com.energy.marketplace.trade.application.command.out.GenerateReceiptCommand;
import com.energy.marketplace.trade.application.exception.TradeSagaProcessingException;
import com.energy.marketplace.trade.application.port.in.HandleBillingSagaEventUseCase;
import com.energy.marketplace.trade.application.port.out.*;
import com.energy.marketplace.trade.domain.exception.InvalidTradeStateException;
import com.energy.marketplace.trade.domain.model.Trade;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReasonCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HandleBillingSagaEventService implements HandleBillingSagaEventUseCase {

    private final LoadTradePort loadTradePort;
    private final SaveTradePort saveTradePort;
    private final SendListingCommandPort sendListingCommandPort;
    private final SendBillingCommandPort sendBillingCommandPort;
    private final TradeStateTransitionRecorder transitionRecorder;
    private final TradeStateUpdateNotifier tradeStateUpdateNotifier;

    @Override
    @Transactional
    public void handlePaymentAuthorized(HandlePaymentAuthorizedCommand command) {
        try {
            Trade trade = loadTradePort.loadTrade(command.tradeId());

            trade.recordPaymentAuthorization(command.paymentAuthorizationId());

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.PAYMENT_AUTHORIZED,
                    trade::markPaymentAuthorized
            );

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.LISTING_CLOSE_REQUESTED,
                    trade::markListingClosingPending
            );

            saveTradePort.save(trade);

            sendListingCommandPort.closeListing(
                    CloseListingCommand.of(
                            trade.getId(),
                            trade.getListingId()
                    )
            );

            tradeStateUpdateNotifier.publishTradeStateUpdate(trade, TradeStateTransitionReasonCode.LISTING_CLOSE_REQUESTED);

        } catch (TradeSagaProcessingException exception) {
            throw exception;
        } catch (InvalidTradeStateException exception) {
            throw new TradeSagaProcessingException(
                    "Cannot handle PaymentAuthorized event because trade state transition is invalid",
                    exception
            );
        } catch (RuntimeException exception) {
            throw new TradeSagaProcessingException(
                    "Failed to handle PaymentAuthorized event for tradeId=" + command.tradeId(),
                    exception
            );
        }
    }

    @Override
    @Transactional
    public void handlePaymentSettled(HandlePaymentSettledCommand command) {
        try {
            Trade trade = loadTradePort.loadTrade(command.tradeId());

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.PAYMENT_SETTLED,
                    trade::markPaymentSettled
            );

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.TRADE_COMPLETED,
                    trade::markCompletedReceiptPending
            );

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.RECEIPT_JOB_REQUESTED,
                    trade::markReceiptGenerationPending
            );

            saveTradePort.save(trade);

            sendBillingCommandPort.generateReceipt(
                    GenerateReceiptCommand.of(
                            trade.getId(),
                            trade.getBuyerId(),
                            trade.getSellerId(),
                            trade.getListingId(),
                            trade.getAmount()
                    )
            );

            tradeStateUpdateNotifier.publishTradeStateUpdate(trade, TradeStateTransitionReasonCode.RECEIPT_JOB_REQUESTED);

        } catch (TradeSagaProcessingException exception) {
            throw exception;
        } catch (InvalidTradeStateException exception) {
            throw new TradeSagaProcessingException(
                    "Cannot handle PaymentSettled event because trade state transition is invalid",
                    exception
            );
        } catch (RuntimeException exception) {
            throw new TradeSagaProcessingException(
                    "Failed to handle PaymentSettled event for tradeId=" + command.tradeId(),
                    exception
            );
        }
    }

    @Override
    @Transactional
    public void handleReceiptGenerated(HandleReceiptGeneratedCommand command) {
        try {
            Trade trade = loadTradePort.loadTrade(command.tradeId());

            trade.recordReceipt(command.receiptId());

            transitionRecorder.transition(
                    trade,
                    TradeStateTransitionReasonCode.RECEIPT_GENERATED,
                    trade::markCompleted
            );

            saveTradePort.save(trade);

            tradeStateUpdateNotifier.publishTradeStateUpdate(trade, TradeStateTransitionReasonCode.RECEIPT_GENERATED);

        } catch (TradeSagaProcessingException exception) {
            throw exception;
        } catch (InvalidTradeStateException exception) {
            throw new TradeSagaProcessingException(
                    "Cannot handle ReceiptGenerated event because trade state transition is invalid",
                    exception
            );
        } catch (RuntimeException exception) {
            throw new TradeSagaProcessingException(
                    "Failed to handle ReceiptGenerated event for tradeId=" + command.tradeId(),
                    exception
            );
        }
    }
}
