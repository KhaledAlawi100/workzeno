
package com.workzeno.workzeno.identity.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.auth.refresh-token")
public record RefreshTokenProperties(Duration ttl) {
}
