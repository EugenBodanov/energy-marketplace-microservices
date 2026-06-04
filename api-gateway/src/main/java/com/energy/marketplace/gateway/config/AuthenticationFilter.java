package com.energy.marketplace.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;

/**
 * JWT Token forwarding filter
 * Extracts Bearer token from incoming request and forwards it to backend services
 */
@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            log.debug("JWT Token found, forwarding to backend service");

            // Token is automatically forwarded by Spring Cloud Gateway
            // No need to do anything, it's included in the request headers
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;  // Execute after logging filter
    }
}

