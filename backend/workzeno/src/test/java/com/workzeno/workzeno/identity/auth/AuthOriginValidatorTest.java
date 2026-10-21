package com.workzeno.workzeno.identity.auth;

import com.workzeno.workzeno.shared.exception.ApiException;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;



class AuthOriginValidatorTest {


    private static final String ALLOWED_ORIGIN = "http://localhost:5173";


    private AuthOriginValidator validator;


    @BeforeEach
    void setUp() {
        validator = new AuthOriginValidator(ALLOWED_ORIGIN);
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST 1 — Happy path: allowed origin should pass
    // ═══════════════════════════════════════════════════════════════════
    // Verifies:
    //   ✔ When the Origin header matches the configured allowlist,
    //     validate() completes WITHOUT throwing.
    //   ✔ Uses assertThatCode(...).doesNotThrowAnyException() — a
    //     readable, self-documenting form of "no throw".
    // ═══════════════════════════════════════════════════════════════════
    @Test
    void validate_shouldAcceptConfiguredFrontendOrigin() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader("Origin", ALLOWED_ORIGIN);

        assertThatCode(() -> validator.validate(request))
                .doesNotThrowAnyException();
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST 2 — Reject: untrusted origin should throw 403
    // ═══════════════════════════════════════════════════════════════════
    // Verifies:
    //   ✔ A foreign origin (evil.com analogue) is rejected.
    //   ✔ Throws ApiException
    //   ✔ ApiException.status == FORBIDDEN (403)
    //   ✔ ApiException.message == "Request origin is not allowed"
    // ═══════════════════════════════════════════════════════════════════
    @Test
    void validate_shouldRejectUntrustedOrigin() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "https://malicious.example");


        assertThatThrownBy(() -> validator.validate(request))

                .isInstanceOf(ApiException.class)

                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.FORBIDDEN);

                    assertThat(apiException.getMessage())
                            .isEqualTo("Request origin is not allowed");
                });
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST 3 — Reject: MISSING Origin header should throw 403
    // ═══════════════════════════════════════════════════════════════════
    // Verifies:
    //   ✔ Absence of Origin is treated as "untrusted" (defense in depth).
    //   ✔ 403 FORBIDDEN is returned.
    //   ✔ Message assertion intentionally omitted → this test focuses
    //     on the STATUS; the message-copy is already pinned in Test 2.
    //     (Slight duplication avoidance.)
    // ═══════════════════════════════════════════════════════════════════
    @Test
    void validate_shouldRejectMissingOrigin() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.FORBIDDEN);
                });
    }
}