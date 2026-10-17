package com.workzeno.workzeno.identity.security;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "workzeno.security.jwt")
public class JwtProperties {

    @NotBlank
    private String secretBase64;

    @NotBlank
    private String issuer;

    @NotNull
    private Duration accessTokenTtl;

    @AssertTrue(message = "access-token-ttl must be positive")
    public boolean isAccessTokenTtlPositive() {
        return accessTokenTtl != null
                && !accessTokenTtl.isZero()
                && !accessTokenTtl.isNegative();
    }
}