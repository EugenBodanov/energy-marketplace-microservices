package com.energy.marketplace.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;

/**
 * Global logging filter for all gateway requests
 * Logs incoming requests and outgoing responses
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        log.info(">>> INCOMING REQUEST <<<");
        log.info("Method: {}", request.getMethod());
        log.info("Path: {}", request.getPath());
        log.info("Query Params: {}", request.getQueryParams());
        log.info("Headers: {}", request.getHeaders());

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    log.info(">>> OUTGOING RESPONSE <<<");
                    log.info("Status: {}", response.getStatusCode());
                    log.info("Headers: {}", response.getHeaders());
                }));
    }

    @Override
    public int getOrder() {
        return -1;  // Execute first
    }
}

