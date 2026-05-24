package com.energy.marketplace.trade.adapter.out.web.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user-service")
public record UserClientProperties (
        String baseUrl
){
}
