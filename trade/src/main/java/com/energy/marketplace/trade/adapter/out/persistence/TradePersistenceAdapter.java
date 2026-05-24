package com.energy.marketplace.trade.adapter.out.persistence;

import com.energy.marketplace.trade.adapter.out.persistence.entity.TradeEntity;
import com.energy.marketplace.trade.adapter.out.persistence.entity.TradeStateHistoryEntity;
import com.energy.marketplace.trade.adapter.out.persistence.mapper.TradePersistenceMapper;
import com.energy.marketplace.trade.adapter.out.persistence.repository.TradeJpaRepository;
import com.energy.marketplace.trade.adapter.out.persistence.repository.TradeStateHistoryJpaRepository;
import com.energy.marketplace.trade.application.port.out.*;
import com.energy.marketplace.trade.domain.model.Trade;
import com.energy.marketplace.trade.domain.model.TradeStateHistory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TradePersistenceAdapter implements
        LoadTradePort,
        SaveTradePort,
        SaveTradeStateHistoryPort {

    private final TradeJpaRepository tradeJpaRepository;
    private final TradeStateHistoryJpaRepository tradeStateHistoryJpaRepository;
    private final TradePersistenceMapper mapper;

    @Override
    public Trade loadTrade(Long tradeId) {
        return tradeJpaRepository.findById(tradeId)
                .map(mapper::toTradeDomain)
                .orElseThrow(() -> new EntityNotFoundException("Trade not found with id: " + tradeId));
    }

    @Override
    public List<Trade> loadTradesByBuyerId(Long buyerId) {
        return tradeJpaRepository.findByBuyerId(buyerId).stream()
                .map(mapper::toTradeDomain)
                .toList();
    }

    @Override
    public Trade save(Trade trade) {
        TradeEntity entity = mapper.toEntity(trade);
        TradeEntity savedEntity = tradeJpaRepository.save(entity);
        return mapper.toTradeDomain(savedEntity);
    }

    @Override
    public TradeStateHistory save(TradeStateHistory tradeStateHistory) {
        TradeStateHistoryEntity entity = mapper.toHistoryEntity(tradeStateHistory);
        TradeStateHistoryEntity savedEntity = tradeStateHistoryJpaRepository.save(entity);
        return mapper.toHistoryDomain(savedEntity);
    }
}
