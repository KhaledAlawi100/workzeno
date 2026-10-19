package com.workzeno.workzeno.identity.bootstrap;

import com.workzeno.workzeno.identity.user.User;
import com.workzeno.workzeno.identity.user.UserRepository;
import com.workzeno.workzeno.identity.user.UserRole;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AdminBootstrapServiceImpl implements AdminBootstrapService {

    /*
     * A PostgreSQL transaction-level advisory lock prevents two bootstrap
     * operations from concurrently creating separate initial administrators.
     * Use the same lock ID for every Workzeno bootstrap deployment.
     */
    private static final long BOOTSTRAP_LOCK_ID = 741029261L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public AdminBootstrapServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void createInitialAdmin(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        validatePassword(rawPassword);

        // Serialize bootstrap attempts within PostgreSQL.
        jdbcTemplate.execute(
                "SELECT pg_advisory_xact_lock(" + BOOTSTRAP_LOCK_ID + ")"
        );

        // Never create another administrator through this bootstrap.
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            throw new IllegalStateException(
                    "An administrator already exists; bootstrap refused"
            );
        }

        // Never silently promote an existing regular account.
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException(
                    "The configured email already belongs to an account; "
                            + "bootstrap refused"
            );
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        User admin = User.createAdmin(normalizedEmail, passwordHash);

        try {
            userRepository.saveAndFlush(admin);
        } catch (DataIntegrityViolationException exception) {
            // Fail closed if a database constraint rejects the insert.
            throw new IllegalStateException(
                    "Initial administrator could not be created",
                    exception
            );
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Bootstrap admin email is required"
            );
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (normalizedEmail.length() > 254
                || !normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Bootstrap admin email must be valid"
            );
        }

        return normalizedEmail;
    }

    private void validatePassword(String rawPassword) {
        if (rawPassword == null
                || rawPassword.length() < 12
                || rawPassword.length() > 72) {
            throw new IllegalArgumentException(
                    "Bootstrap password must be between 12 and 72 characters"
            );
        }
    }
}