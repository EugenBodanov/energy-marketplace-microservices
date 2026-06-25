package com.energy.marketplace.trade.adapter.out.persistence.entity;

import com.energy.marketplace.trade.domain.model.TradeStatus;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReasonCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = true, length = 64)
    private TradeStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 64)
    private TradeStatus toStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 128)
    private TradeStateTransitionReasonCode reasonCode;

    @Column(columnDefinition = "TEXT")
    private String reasonDetails;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(nullable = false)
    private Instant changedAt;

}
