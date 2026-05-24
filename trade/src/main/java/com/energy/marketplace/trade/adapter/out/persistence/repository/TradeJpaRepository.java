package com.energy.marketplace.trade.adapter.out.persistence.repository;

import com.energy.marketplace.trade.adapter.out.persistence.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeJpaRepository extends JpaRepository<TradeEntity, Long> {
    List<TradeEntity> findByBuyerId(Long buyerId);
}
