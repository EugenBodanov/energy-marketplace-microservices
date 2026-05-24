package com.energy.marketplace.trade.adapter.out.web.config;

import com.energy.marketplace.trade.adapter.out.web.property.UserClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(UserClientProperties.class)
public class UserClientConfig {

    private final UserClientProperties properties;

    @Bean
    public RestClient userRestClient() {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }
}
