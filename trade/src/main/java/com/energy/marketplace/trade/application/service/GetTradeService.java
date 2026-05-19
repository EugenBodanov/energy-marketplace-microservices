package com.energy.marketplace.trade.application.service;

import com.energy.marketplace.trade.application.command.in.GetTradeCommand;
import com.energy.marketplace.trade.application.port.in.GetTradeUseCase;
import com.energy.marketplace.trade.application.port.out.LoadTradePort;
import com.energy.marketplace.trade.application.result.GetTradeResult;
import com.energy.marketplace.trade.domain.model.Trade;
import org.springframework.stereotype.Service;

@Service
@lombok.RequiredArgsConstructor
public class GetTradeService implements GetTradeUseCase {

    private final LoadTradePort loadTradePort;

    @Override
    public GetTradeResult getTrade(GetTradeCommand command) {
        Trade trade = loadTradePort.load(command.tradeId());
        return new GetTradeResult(trade.getId(), trade.getBuyerId(), trade.getSellerId(), trade.getListingId(),
                trade.getAmount(), trade.getStatus());
    }
}
