package com.workzeno.workzeno.shared.openapi;

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
        summary = "Register a new user",
        description = "Creates a new user account."
)
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "User registered successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid registration data",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "Account already exists",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        )
})
public @interface RegisterOperation {
}