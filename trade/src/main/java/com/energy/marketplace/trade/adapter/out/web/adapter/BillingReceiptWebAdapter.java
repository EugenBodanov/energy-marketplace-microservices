package com.energy.marketplace.trade.adapter.out.web.adapter;

import com.energy.marketplace.trade.application.port.out.RequestReceiptPort;
import com.energy.marketplace.trade.application.result.GetReceiptResult;

import com.energy.marketplace.trade.adapter.out.web.dto.BillingReceiptResponse;
import com.energy.marketplace.trade.adapter.out.web.exception.BillingReceiptClientException;
import com.energy.marketplace.trade.adapter.out.web.exception.ReceiptNotFoundException;
import com.energy.marketplace.trade.adapter.out.web.mapper.BillingReceiptWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BillingReceiptWebAdapter extends Adapter implements RequestReceiptPort {

    private final RestClient billingRestClient;
    private final BillingReceiptWebMapper mapper;

    @Override
    public GetReceiptResult requestReceiptById(Long tradeId) {
        BillingReceiptResponse response = billingRestClient.get()
                .uri("/billing/receipts/{tradeId}", tradeId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, clientResponse) -> {
                    throw new ReceiptNotFoundException(tradeId);
                })
                .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                    throw new BillingReceiptClientException(
                            "Billing Service returned error while requesting receipt for tradeId=%d. Status=%s. Body=%s"
                                    .formatted(
                                            tradeId,
                                            clientResponse.getStatusCode(),
                                            readBody(clientResponse)
                                    )
                    );
                })
                .body(BillingReceiptResponse.class);

        return mapper.toResult(Objects.requireNonNull(response));
    }
}
