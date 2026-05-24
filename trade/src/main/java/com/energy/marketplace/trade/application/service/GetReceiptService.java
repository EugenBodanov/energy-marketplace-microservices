package com.energy.marketplace.trade.application.service;

import com.energy.marketplace.trade.application.command.in.GetReceiptCommand;
import com.energy.marketplace.trade.application.exception.ReceiptNotReadyException;
import com.energy.marketplace.trade.application.exception.TradeSagaProcessingException;
import com.energy.marketplace.trade.application.port.in.GetReceiptUseCase;
import com.energy.marketplace.trade.application.port.out.RequestReceiptPort;
import com.energy.marketplace.trade.application.port.out.LoadTradePort;
import com.energy.marketplace.trade.application.result.GetReceiptResult;
import com.energy.marketplace.trade.domain.model.Trade;
import com.energy.marketplace.trade.domain.model.TradeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetReceiptService implements GetReceiptUseCase {

    private final LoadTradePort loadTradePort;
    private final RequestReceiptPort requestReceiptPort;

    @Override
    public GetReceiptResult getReceipt(GetReceiptCommand command) {
        try {
            Trade trade = loadTradePort.loadTrade(command.tradeId());

            if (trade.getStatus() != TradeStatus.COMPLETED) {
                throw new ReceiptNotReadyException(
                        "Receipt is not ready because trade is not completed. Current status: "
                                + trade.getStatus()
                );
            }

            if (trade.getReceiptId() == null) {
                throw new ReceiptNotReadyException(
                        "Receipt is not ready because receipt id is missing for tradeId="
                                + trade.getId()
                );
            }

            return requestReceiptPort.requestReceiptById(trade.getReceiptId());

        } catch (ReceiptNotReadyException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new TradeSagaProcessingException(
                    "Failed to get receipt for tradeId=" + command.tradeId(),
                    exception
            );
        }
    }
}
