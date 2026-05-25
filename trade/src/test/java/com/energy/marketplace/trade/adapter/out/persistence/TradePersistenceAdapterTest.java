package com.energy.marketplace.trade.adapter.out.persistence;

import com.energy.marketplace.trade.adapter.out.persistence.entity.TradeEntity;
import com.energy.marketplace.trade.adapter.out.persistence.mapper.TradePersistenceMapper;
import com.energy.marketplace.trade.adapter.out.persistence.repository.TradeJpaRepository;
import com.energy.marketplace.trade.adapter.out.persistence.repository.TradeStateHistoryJpaRepository;
import com.energy.marketplace.trade.domain.model.Trade;
import com.energy.marketplace.trade.domain.valueObject.Money;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradePersistenceAdapterTest {

    @Mock
    private TradeJpaRepository tradeJpaRepository;
    @Mock
    private TradeStateHistoryJpaRepository tradeStateHistoryJpaRepository;
    @Mock
    private TradePersistenceMapper mapper;

    private TradePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TradePersistenceAdapter(tradeJpaRepository, tradeStateHistoryJpaRepository, mapper);
    }

    @Test
    @DisplayName("Should load trade when exists")
    void shouldLoadTrade() {
        Long tradeId = 1L;
        TradeEntity entity = new TradeEntity();
        Trade trade = Trade.createTrade(1L, 2L, 3L, Money.of("10.00", "EUR"));

        when(tradeJpaRepository.findById(tradeId)).thenReturn(Optional.of(entity));
        when(mapper.toTradeDomain(entity)).thenReturn(trade);

        Trade result = adapter.loadTrade(tradeId);

        assertNotNull(result);
        assertEquals(trade, result);
    }

    @Test
    @DisplayName("Should throw exception when trade not found")
    void shouldThrowExceptionWhenNotFound() {
        when(tradeJpaRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> adapter.loadTrade(1L));
    }

    @Test
    @DisplayName("Should save trade")
    void shouldSaveTrade() {
        Trade trade = Trade.createTrade(1L, 2L, 3L, Money.of("10.00", "EUR"));
        TradeEntity entity = new TradeEntity();

        when(mapper.toEntity(trade)).thenReturn(entity);
        when(tradeJpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toTradeDomain(entity)).thenReturn(trade);

        Trade result = adapter.save(trade);

        assertEquals(trade, result);
        verify(tradeJpaRepository).save(entity);
    }
}
