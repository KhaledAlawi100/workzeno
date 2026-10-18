
package com.workzeno.workzeno.identity.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.auth.refresh-token")
public record RefreshTokenProperties(Duration ttl) {
}
