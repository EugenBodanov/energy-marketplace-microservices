package com.energy.marketplace.trade.application.service;

import com.energy.marketplace.trade.application.command.in.CreateTradeCommand;
import com.energy.marketplace.trade.application.port.in.CreateTradeUseCase;
import com.energy.marketplace.trade.application.port.out.SaveTradePort;
import com.energy.marketplace.trade.application.result.CreateTradeResult;
import com.energy.marketplace.trade.domain.model.Trade;

public class CreateTradeService implements CreateTradeUseCase {

    private final SaveTradePort saveTradePort;

    public CreateTradeService(SaveTradePort saveTradePort) {
        this.saveTradePort = saveTradePort;
    }

    @Override
    public CreateTradeResult createTrade(CreateTradeCommand command) {
        Trade trade = saveTradePort.save(Trade.createTrade(command.buyerId(), command.sellerId(), command.listingId(), command.amount()));
        return new CreateTradeResult(trade.getId(), trade.getStatus());
    }
}
