package com.energy.marketplace.gateway;

import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableConfigurationProperties(AppGatewayProperties.class)
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Highly optimized route locator using path-pattern routing predicates.
     * This eliminates boilerplate route additions whenever microservices add endpoints.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AppGatewayProperties gatewayProperties) {
        return builder.routes()
                // Route ALL user service traffic dynamically using wildcards (**)
                .route("user-service-route", r -> r
                        .path("/users/**")
                        .uri(gatewayProperties.getServices().getUserService().getUrl()))

                // Route ALL listing service traffic dynamically
                .route("listing-service-route", r -> r
                        .path("/listings/**")
                        .uri(gatewayProperties.getServices().getListingService().getUrl()))

                // Route ALL trade service traffic dynamically
                .route("trade-service-route", r -> r
                        .path("/trades/**")
                        .uri(gatewayProperties.getServices().getTradeService().getUrl()))

                // Standard actuator/local health tracking
                .route("health-route", r -> r
                        .path("/health")
                        .uri("no://op"))
                .build();
    }
}

/**
 * Configuration Properties for Service URLs
 * Allows dynamic configuration per environment via application.yaml or environment variables
 */
@ConfigurationProperties(prefix = "gateway")
@Data
class AppGatewayProperties {
    private Services services = new Services();

    @Data
    public static class Services {
        private ServiceConfig userService;
        private ServiceConfig listingService;
        private ServiceConfig tradeService;
    }

    @Data
    public static class ServiceConfig {
        private String url;
    }
}

