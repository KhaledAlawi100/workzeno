package com.workzeno.workzeno.shared.openapi;

import com.workzeno.workzeno.shared.api.ApiResponse; // Import your own ApiResponse
import com.workzeno.workzeno.shared.openapi.schema.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Login",
        description = "Authenticates a user and returns an access token. A refresh token is issued through an HTTP-only cookie."
)
@ApiResponses({
        // Use the fully qualified name here to reference Swagger's ApiResponse
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.class) // Your own ApiResponse
                )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid request",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        )
})
public @interface LoginOperation {
}