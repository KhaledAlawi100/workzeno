
package com.workzeno.workzeno.identity.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "workzeno.admin-password-reset",
        name = "enabled",
        havingValue = "true"
)
public class AdminPasswordResetRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminPasswordResetRunner(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${workzeno.admin-password-reset.email:}") String email,
            @Value("${workzeno.admin-password-reset.password:}") String password) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Admin reset email is required");
        }

        if (password == null || password.length() < 12 || password.length() > 72) {
            throw new IllegalStateException(
                    "Password must be between 12 and 72 characters"
            );
        }

        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        String passwordHash = passwordEncoder.encode(password);

        int updated = jdbcTemplate.update(
                """
                UPDATE users
                SET password_hash = ?
                WHERE LOWER(email) = ?
                  AND role = 'ADMIN'
                  AND status = 'ACTIVE'
                """,
                passwordHash,
                normalizedEmail
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Expected exactly one active admin account; updated rows: "
                            + updated
            );
        }

        System.out.println(
                "Admin password reset successfully for " + normalizedEmail
        );
    }
}
