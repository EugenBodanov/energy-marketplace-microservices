package com.energy.marketplace.trade.adapter.out.persistence.entity;

import com.energy.marketplace.trade.domain.model.TradeStatus;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReasonCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "trade_state_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeStateHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tradeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TradeStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStateTransitionReasonCode reasonCode;

    @Column(columnDefinition = "TEXT")
    private String reasonDetails;

    @Column(nullable = false)
    private Instant changedAt;

}
