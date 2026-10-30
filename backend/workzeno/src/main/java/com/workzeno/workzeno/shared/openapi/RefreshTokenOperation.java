package com.workzeno.workzeno.shared.openapi;

import com.workzeno.workzeno.identity.auth.dto.AuthResponse;
import com.workzeno.workzeno.shared.openapi.schema.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Refresh access token",
        description = "Uses the refresh token stored in an HTTP-only cookie to issue a new access token and rotate the refresh token."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Token refreshed successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AuthResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Refresh token is missing, invalid, expired, or revoked",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        )
})
public @interface RefreshTokenOperation {
}