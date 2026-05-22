package com.energy.marketplace.trade.adapter.in.web;

import com.energy.marketplace.trade.adapter.in.web.dto.CreateTradeRequest;
import com.energy.marketplace.trade.adapter.in.web.dto.CreateTradeResponse;
import com.energy.marketplace.trade.adapter.in.web.dto.GetReceiptResponse;
import com.energy.marketplace.trade.adapter.in.web.dto.GetTradeResponse;
import com.energy.marketplace.trade.application.command.in.CreateTradeCommand;
import com.energy.marketplace.trade.application.result.CreateTradeResult;
import com.energy.marketplace.trade.application.service.CreateTradeService;
import com.energy.marketplace.trade.application.service.GetReceiptService;
import com.energy.marketplace.trade.application.service.GetTradeService;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@NoArgsConstructor
@RequestMapping("/trades")
public class TradeController {

    CreateTradeService createTradeService;
    TradeWebMapper tradeWebMapper;
    GetTradeService getTradeService;
    GetReceiptService getReceiptService;

    @PostMapping
    public CreateTradeResponse createTrade(@Valid @RequestBody CreateTradeRequest request) {
        return tradeWebMapper.toCreateTradeResponse(createTradeService.createTrade(tradeWebMapper.toCreateTradeCommand(request)));
    }

    @GetMapping("/{tradeId}")
    public GetTradeResponse getTrade(@PathVariable("tradeId") Long tradeId) {
        return tradeWebMapper.toGetTradeResponse(getTradeService.getTrade(tradeWebMapper.toGetTradeCommand(tradeId)));
    }

    @GetMapping("/{buyerId}")
    public List<GetTradeResponse> getTrades(@PathVariable("buyerId") Long buyerId) {
        return getTradeService.getTradesByBuyerId(buyerId).stream().map(tradeWebMapper::toGetTradeResponse).toList();
    }

    @GetMapping("/trades/{tradeId}/receipt")
    public GetReceiptResponse getReceipt(@PathVariable("tradeId") Long tradeId){
        return tradeWebMapper.toGetReceiptResponse(getReceiptService.getReceipt(
                tradeWebMapper.toGetReceiptCommand(tradeId)));
    }

}
