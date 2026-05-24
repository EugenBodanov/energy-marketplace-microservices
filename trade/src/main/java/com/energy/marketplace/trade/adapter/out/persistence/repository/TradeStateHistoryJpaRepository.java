package com.energy.marketplace.trade.adapter.out.persistence.repository;

import com.energy.marketplace.trade.adapter.out.persistence.entity.TradeStateHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeStateHistoryJpaRepository extends JpaRepository<TradeStateHistoryEntity, Long> {
}
