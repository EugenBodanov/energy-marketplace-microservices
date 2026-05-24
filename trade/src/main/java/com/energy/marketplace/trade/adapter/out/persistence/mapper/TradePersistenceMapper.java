package com.energy.marketplace.trade.adapter.out.persistence.mapper;

import com.energy.marketplace.trade.adapter.out.persistence.entity.TradeEntity;
import com.energy.marketplace.trade.adapter.out.persistence.entity.TradeStateHistoryEntity;
import com.energy.marketplace.trade.domain.model.Trade;
import com.energy.marketplace.trade.domain.model.TradeStateHistory;
import com.energy.marketplace.trade.domain.valueObject.Money;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReason;
import com.energy.marketplace.trade.domain.valueObject.TradeStateTransitionReasonCode;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class TradePersistenceMapper {

    public TradeEntity toEntity(Trade trade) {
        if (trade == null) return null;
        return TradeEntity.builder()
                .id(trade.getId())
                .buyerId(trade.getBuyerId())
                .sellerId(trade.getSellerId())
                .listingId(trade.getListingId())
                .amount(trade.getAmount().amount())
                .currencyCode(trade.getAmount().currencyCode())
                .status(trade.getStatus())
                .paymentAuthorizationId(trade.getPaymentAuthorizationId())
                .receiptId(trade.getReceiptId())
                .build();
    }

    public Trade toTradeDomain(TradeEntity entity) {
        if (entity == null) return null;
        Trade trade = new Trade(
                entity.getId(),
                entity.getBuyerId(),
                entity.getSellerId(),
                entity.getListingId(),
                new Money(entity.getAmount(), Currency.getInstance(entity.getCurrencyCode())),
                entity.getStatus()
        );
        if (entity.getPaymentAuthorizationId() != null) {
            trade.recordPaymentAuthorization(entity.getPaymentAuthorizationId());
        }
        if (entity.getReceiptId() != null) {
            trade.recordReceipt(entity.getReceiptId());
        }
        return trade;
    }

    public TradeStateHistoryEntity toHistoryEntity(TradeStateHistory history) {
        if (history == null) return null;
        return TradeStateHistoryEntity.builder()
                .tradeId(history.getTradeId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .reasonCode(history.getReason().code())
                .reasonDetails(history.getReason().details())
                .changedAt(history.getChangedAt())
                .build();
    }

    public TradeStateHistory toHistoryDomain(TradeStateHistoryEntity entity) {
        if (entity == null) return null;
        return new TradeStateHistory(
                entity.getTradeId(),
                entity.getFromStatus(),
                entity.getToStatus(),
                TradeStateTransitionReason.of(entity.getReasonCode(), entity.getReasonDetails()),
                entity.getChangedAt()
        );
    }
}
