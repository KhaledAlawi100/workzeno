package com.workzeno.workzeno.identity.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "workzeno.bootstrap-admin")
public record BootstrapAdminProperties(
        boolean enabled,
        String email,
        String password
) {
}