package com.workzeno.workzeno.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Workzeno API",
                version = "v1",
                description = """
                        REST API for Workzeno.
                        Provides authentication and project-management capabilities.
                        """,
                contact = @Contact(
                        name = "Workzeno Development Team"
                )
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter the access token. DO NOT include the 'Bearer ' prefix."
)
@SecurityScheme(
        name = "refreshCookie",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "refreshToken",
        description = "Refresh token cookie issued by the login endpoint."
)
public class OpenApiConfig {
}
