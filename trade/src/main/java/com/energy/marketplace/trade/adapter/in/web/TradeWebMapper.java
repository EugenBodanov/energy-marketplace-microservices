package com.energy.marketplace.trade.adapter.in.web;

import com.energy.marketplace.trade.adapter.in.web.dto.CreateTradeRequest;
import com.energy.marketplace.trade.adapter.in.web.dto.CreateTradeResponse;
import com.energy.marketplace.trade.adapter.in.web.dto.GetReceiptResponse;
import com.energy.marketplace.trade.adapter.in.web.dto.GetTradeResponse;
import com.energy.marketplace.trade.application.command.in.CreateTradeCommand;
import com.energy.marketplace.trade.application.command.in.GetReceiptCommand;
import com.energy.marketplace.trade.application.command.in.GetTradeCommand;
import com.energy.marketplace.trade.application.result.CreateTradeResult;
import com.energy.marketplace.trade.application.result.GetReceiptResult;
import com.energy.marketplace.trade.application.result.GetTradeResult;
import com.energy.marketplace.trade.domain.valueObject.Money;
import org.springframework.stereotype.Component;

@Component
public class TradeWebMapper {

    public CreateTradeCommand toCreateTradeCommand(CreateTradeRequest request) {
        return new CreateTradeCommand(
                request.buyerId(),
                request.sellerId(),
                request.listingId(),
                Money.of(request.amount(), request.currency())
        );
    }

    public GetTradeCommand toGetTradeCommand(Long tradeId) {
        return new GetTradeCommand(tradeId);
    }

    public GetReceiptCommand toGetReceiptCommand(Long tradeId) {
        return new GetReceiptCommand(tradeId);
    }

    public CreateTradeResponse toCreateTradeResponse(CreateTradeResult result) {
        return new CreateTradeResponse(
                result.tradeId(),
                result.status()
        );
    }

    public GetTradeResponse toGetTradeResponse(GetTradeResult result) {
        return new GetTradeResponse(
                result.tradeId(),
                result.buyerId(),
                result.sellerId(),
                result.listingId(),
                result.amount().amount(),
                result.amount().currencyCode(),
                result.status()
        );
    }

    public GetReceiptResponse toGetReceiptResponse(GetReceiptResult result) {
        return new GetReceiptResponse(result.tradeId(), result.receiptId(), result.buyerId(), result.sellerId(),
                result.listingId(), result.amount().amount(), result.amount().currencyCode(), result.generatedAt());
    }
}