package com.seedit.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
    String securityJwtName = "JWT Bearer Token";

    // 1. SecurityRequirement 설정 (모든 API 문서에 자물쇠 아이콘 적용)
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityJwtName);

    // 2. SecurityScheme 설정 (헤더에 Authorization: Bearer <Token> 형태로 들어가도록 규격 선언)
    SecurityScheme securityScheme = new SecurityScheme()
            .name(securityJwtName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Seedit")
                        .version("1.0")
                        .description("청소년을 위한 모의주식투자 다이어리 API 문서"))
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes(securityJwtName, securityScheme));
    }
}
