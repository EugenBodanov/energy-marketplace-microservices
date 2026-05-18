package com.energy.marketplace.trade.application.port.in;

import com.energy.marketplace.trade.application.command.in.CreateTradeCommand;
import com.energy.marketplace.trade.application.result.CreateTradeResult;

public interface CreateTradeUseCase {

    CreateTradeResult createTrade(CreateTradeCommand command);

}
