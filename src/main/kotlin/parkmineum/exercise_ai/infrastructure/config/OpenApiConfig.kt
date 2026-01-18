package parkmineum.exercise_ai.infrastructure.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger(OpenAPI) 설정 클래스
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "BearerAuth"
        
        return OpenAPI()
            .info(Info()
                .title("API 명세서")
                .version("v1.0.0"))
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(Components()
                .addSecuritySchemes(securitySchemeName, SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
    }
}