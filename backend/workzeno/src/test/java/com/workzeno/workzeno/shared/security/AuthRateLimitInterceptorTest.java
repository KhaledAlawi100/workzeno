package com.workzeno.workzeno.shared.security;

import com.workzeno.workzeno.shared.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthRateLimitInterceptorTest {

    private AuthRateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AuthRateLimitInterceptor();
    }

    @Test
    void login_shouldAllowFiveRequestsFromSameIp() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = request(
                    "/api/v1/auth/login",
                    "192.0.2.10"
            );

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            assertThat(interceptor.preHandle(
                    request, response, new Object()
            )).isTrue();
        }
    }

    @Test
    void login_shouldRejectSixthRequestAndSetRetryAfter() throws Exception {
        for (int i = 0; i < 5; i++) {
            interceptor.preHandle(
                    request("/api/v1/auth/login", "192.0.2.11"),
                    new MockHttpServletResponse(),
                    new Object()
            );
        }

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        assertThatThrownBy(() ->
                interceptor.preHandle(
                        request("/api/v1/auth/login", "192.0.2.11"),
                        response,
                        new Object()
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                });

        assertThat(response.getHeader("Retry-After"))
                .isNotNull()
                .isNotEqualTo("0");
    }

    @Test
    void login_shouldTrackDifferentIpsIndependently() throws Exception {
        for (int i = 0; i < 5; i++) {
            interceptor.preHandle(
                    request("/api/v1/auth/login", "192.0.2.12"),
                    new MockHttpServletResponse(),
                    new Object()
            );
        }

        assertThat(interceptor.preHandle(
                request("/api/v1/auth/login", "192.0.2.13"),
                new MockHttpServletResponse(),
                new Object()
        )).isTrue();
    }

    @Test
    void differentEndpoints_shouldHaveIndependentLimits() throws Exception {
        for (int i = 0; i < 5; i++) {
            interceptor.preHandle(
                    request("/api/v1/auth/login", "192.0.2.14"),
                    new MockHttpServletResponse(),
                    new Object()
            );
        }

        assertThat(interceptor.preHandle(
                request("/api/v1/auth/register", "192.0.2.14"),
                new MockHttpServletResponse(),
                new Object()
        )).isTrue();
    }

    @Test
    void nonPostRequest_shouldNotBeLimited() throws Exception {
        MockHttpServletRequest request =
                request("/api/v1/auth/login", "192.0.2.15");

        request.setMethod("GET");

        assertThat(interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                new Object()
        )).isTrue();
    }

    private MockHttpServletRequest request(String path, String ip) {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("POST");
        request.setRequestURI(path);
        request.setServletPath(path);
        request.setRemoteAddr(ip);

        return request;
    }
}