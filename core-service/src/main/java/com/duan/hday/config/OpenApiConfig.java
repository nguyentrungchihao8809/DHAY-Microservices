package com.duan.hday.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 1. Định nghĩa Server là Gateway (localhost:8000) thay vì IP Docker
        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:8000");
        gatewayServer.setDescription("API Gateway");

        // 2. Cấu hình Security (JWT Bearer Auth)
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info().title("Dhay Core API").version("1.0"))
                .servers(List.of(gatewayServer)) // Thêm Server vào đây
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // Sửa đoạn này trong file của bạn
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new io.swagger.v3.oas.models.security.SecurityScheme() // Chỉ định rõ class model
                                        .name(securitySchemeName)
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP) // Dùng Type.HTTP
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}