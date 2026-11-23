package com.workzeno.workzeno.identity.user.domain;

import java.util.Locale;

/**
 * Centralizes canonical email representation.
 *
 * Email addresses are trimmed and lowercased using Locale.ROOT
 * so account lookup and uniqueness checks behave consistently.
 */

public final class EmailAddress {

    private EmailAddress() {
    }

    /**
     * Normalizes an email address by trimming whitespace and converting to lowercase.
     *
     * @param email the email address to normalize
     * @return the normalized email address
     * @throws IllegalArgumentException if the email is null or blank
     */

    public static String normalize(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}