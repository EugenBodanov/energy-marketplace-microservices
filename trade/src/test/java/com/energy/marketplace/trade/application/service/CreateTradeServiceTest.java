package com.energy.marketplace.trade.application.service;

import com.energy.marketplace.trade.application.command.in.CreateTradeCommand;
import com.energy.marketplace.trade.application.port.out.SaveTradePort;
import com.energy.marketplace.trade.application.port.out.SendListingCommandPort;
import com.energy.marketplace.trade.application.port.out.ValidateTradeParticipantsPort;
import com.energy.marketplace.trade.application.result.CreateTradeResult;
import com.energy.marketplace.trade.domain.model.Trade;
import com.energy.marketplace.trade.domain.model.TradeStatus;
import com.energy.marketplace.trade.domain.valueObject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTradeServiceTest {

    private SaveTradePort saveTradePort = mock(SaveTradePort.class);
    private ValidateTradeParticipantsPort validateTradeParticipantsPort = mock(ValidateTradeParticipantsPort.class);
    private SendListingCommandPort sendListingCommandPort = mock(SendListingCommandPort.class);
    private TradeStateTransitionRecorder transitionRecorder = mock(TradeStateTransitionRecorder.class);

    private CreateTradeService createTradeService;

    private CreateTradeCommand command;
    private Money amount;

    @BeforeEach
    void setUp() {
        createTradeService = new CreateTradeService(saveTradePort, validateTradeParticipantsPort, sendListingCommandPort, transitionRecorder);
        amount = Money.of("100.00", "EUR");
        command = new CreateTradeCommand(1L, 2L, 3L, amount);
    }

    @Test
    @DisplayName("Should successfully create trade when participants are valid")
    void shouldCreateTradeSuccessfully() {
        Trade tradeWithId = new Trade(100L, 1L, 2L, 3L, amount, TradeStatus.CREATED);

        when(saveTradePort.save(any(Trade.class))).thenReturn(tradeWithId);
        when(validateTradeParticipantsPort.validateBuyer(1L)).thenReturn(true);
        when(validateTradeParticipantsPort.validateSeller(2L)).thenReturn(true);

        // Stub transitionRecorder to actually run the status change runnable
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(2);
            runnable.run();
            return null;
        }).when(transitionRecorder).transition(any(), any(), any());

        CreateTradeResult result = createTradeService.createTrade(command);

        assertEquals(100L, result.tradeId());
        assertEquals(TradeStatus.LISTING_RESERVATION_PENDING, result.status());

        verify(saveTradePort, atLeastOnce()).save(any(Trade.class));
        verify(sendListingCommandPort).reserveListing(any());
        verify(transitionRecorder, times(3)).transition(any(), any(), any());
    }

    @Test
    @DisplayName("Should fail trade creation when participants are invalid")
    void shouldFailTradeCreationWhenParticipantsInvalid() {
        Trade tradeWithId = new Trade(100L, 1L, 2L, 3L, amount, TradeStatus.CREATED);

        when(saveTradePort.save(any(Trade.class))).thenReturn(tradeWithId);
        when(validateTradeParticipantsPort.validateBuyer(1L)).thenReturn(false);
        when(validateTradeParticipantsPort.validateSeller(2L)).thenReturn(true);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(2);
            runnable.run();
            return null;
        }).when(transitionRecorder).transition(any(), any(), any());

        CreateTradeResult result = createTradeService.createTrade(command);

        assertEquals(100L, result.tradeId());
        assertEquals(TradeStatus.FAILED, result.status());

        verify(sendListingCommandPort, never()).reserveListing(any());
    }
}
