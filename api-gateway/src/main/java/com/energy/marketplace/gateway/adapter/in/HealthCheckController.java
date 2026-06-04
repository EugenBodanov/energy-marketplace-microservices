package com.energy.marketplace.gateway.adapter.in;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint for API Gateway
 */
@RestController
public class HealthCheckController {

    /**
     * Simple health check endpoint
     * @return Health status
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "api-gateway");
        response.put("timestamp", System.currentTimeMillis());

        return Mono.just(response);
    }

    /**
     * Gateway info endpoint
     * @return Gateway information
     */
    @GetMapping("/info")
    public Mono<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Energy Marketplace API Gateway");
        response.put("version", "1.0.0");
        response.put("description", "Central entry point for all microservices");

        Map<String, String> routes = new HashMap<>();
        routes.put("users", "/users/**");
        routes.put("listings", "/listings/**");
        routes.put("trades", "/trades/**");
        response.put("routes", routes);

        return Mono.just(response);
    }
}

