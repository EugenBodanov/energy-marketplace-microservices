package com.energy.marketplace.trade.adapter.out.web.config;

import com.energy.marketplace.trade.adapter.out.web.property.BillingReceiptClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(BillingReceiptClientProperties.class)
public class BillingReceiptClientConfig {

    private final BillingReceiptClientProperties properties;

    @Bean
    public RestClient billingRestClient() {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }
}