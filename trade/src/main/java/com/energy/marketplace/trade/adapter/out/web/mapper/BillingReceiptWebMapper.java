package com.energy.marketplace.trade.adapter.out.web.mapper;

import com.energy.marketplace.trade.adapter.out.web.dto.BillingReceiptResponse;
import com.energy.marketplace.trade.application.result.GetReceiptResult;
import com.energy.marketplace.trade.domain.valueObject.Money;
import org.springframework.stereotype.Component;

@Component
public class BillingReceiptWebMapper {

    public GetReceiptResult toResult(BillingReceiptResponse response) {
        return new GetReceiptResult(
                response.tradeId(),
                response.receiptId(),
                response.buyerId(),
                response.sellerId(),
                response.listingId(),
                Money.of(response.amount(), response.currency()),
                response.generatedAt()
        );
    }
}
