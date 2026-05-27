package com.energy.marketplace.gateway.config;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ErrorFilter implements ErrorWebExceptionHandler {

    private final List<HttpMessageWriter<?>> messageWriters =
            HandlerStrategies.withDefaults().messageWriters();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway error occurred: ", ex);
        HttpStatus status = resolveStatus(ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", exchange.getRequest().getPath().value());

        // Use clean, non-blocking functional ServerResponse to write out JSON data natively
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse)
                .flatMap(serverResponse -> serverResponse.writeTo(exchange, new HandlerContext()));
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("404")) return HttpStatus.NOT_FOUND;
        if (ex.getMessage() != null && ex.getMessage().contains("401")) return HttpStatus.UNAUTHORIZED;
        if (ex.getMessage() != null && ex.getMessage().contains("403")) return HttpStatus.FORBIDDEN;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    // Inner helper class required by ServerResponse to resolve message codecs safely
    private class HandlerContext implements ServerResponse.Context {
        @Override
        public List<HttpMessageWriter<?>> messageWriters() { return messageWriters; }
        @Override
        public List<org.springframework.web.reactive.result.view.ViewResolver> viewResolvers() { return List.of(); }
    }
}