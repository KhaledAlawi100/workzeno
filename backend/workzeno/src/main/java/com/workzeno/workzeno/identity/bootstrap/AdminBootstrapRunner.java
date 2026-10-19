package com.workzeno.workzeno.identity.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "workzeno.bootstrap-admin",
        name = "enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AdminBootstrapService adminBootstrapService;
    private final BootstrapAdminProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (properties.email() == null || properties.email().isBlank()) {
            throw new IllegalStateException(
                    "Bootstrap admin email must be configured"
            );
        }

        if (properties.password() == null || properties.password().isBlank()) {
            throw new IllegalStateException(
                    "Bootstrap admin password must be configured"
            );
        }

        adminBootstrapService.createInitialAdmin(
                properties.email(),
                properties.password()
        );
    }
}