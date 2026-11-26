package com.workzeno.workzeno.shared.config;

import com.workzeno.workzeno.shared.security.AuthRateLimitInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String frontendOrigin;
    private final AuthRateLimitInterceptor authRateLimitInterceptor;

    public WebConfig(
            @Value("${app.cors.allowed-origin}") String frontendOrigin,
            AuthRateLimitInterceptor authRateLimitInterceptor
    ) {
        this.frontendOrigin = frontendOrigin;
        this.authRateLimitInterceptor = authRateLimitInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(frontendOrigin)
                .allowedMethods(
                        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
                )
                .allowedHeaders(
                        "Content-Type", "Authorization", "X-Requested-With"
                )
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authRateLimitInterceptor)
                .addPathPatterns(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/refresh"
                );
    }
}