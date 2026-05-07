package com.energy.marketplace.listing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI listingServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Listing Service API")
                        .description("Energy listing management, search, and reservation API.")
                        .version("v1"));
    }

    @Bean
    public GroupedOpenApi listingServiceApiGroup() {
        return GroupedOpenApi.builder()
                .group("listing-service")
                .pathsToMatch("/listings", "/listings/**")
                .build();
    }
}
