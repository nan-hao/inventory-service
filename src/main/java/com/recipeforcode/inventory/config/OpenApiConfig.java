package com.recipeforcode.inventory.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inventory Service API",
                version = "v1",
                description = "APIs to reserve and confirm inventory items with idempotency."
        )
)
public class OpenApiConfig {
}
