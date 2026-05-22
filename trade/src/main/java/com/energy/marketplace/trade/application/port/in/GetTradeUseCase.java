package com.energy.marketplace.trade.application.port.in;

import com.energy.marketplace.trade.application.command.in.GetTradeCommand;
import com.energy.marketplace.trade.application.result.GetTradeResult;

import java.util.List;

public interface GetTradeUseCase {

    GetTradeResult getTrade(GetTradeCommand command);

    List<GetTradeResult> getTradesByBuyerId(Long buyerId);
}
