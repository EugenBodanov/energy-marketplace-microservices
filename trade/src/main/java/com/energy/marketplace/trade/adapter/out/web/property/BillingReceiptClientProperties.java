package com.energy.marketplace.trade.adapter.out.web.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "billing-service")
public record BillingReceiptClientProperties (
        String baseUrl
){
}
