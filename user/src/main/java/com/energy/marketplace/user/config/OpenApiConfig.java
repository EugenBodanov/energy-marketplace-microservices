package com.energy.marketplace.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("User registration, login, lookup, and trade validation API.")
                        .version("v1"));
    }

    @Bean
    public GroupedOpenApi userServiceApiGroup() {
        return GroupedOpenApi.builder()
                .group("user-service")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }
}
