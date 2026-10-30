package com.workzeno.workzeno.shared.openapi;

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
        summary = "Logout",
        description = "Revokes the refresh token, if present, and clears the refresh-token cookie."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Logout successful"
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Request origin is not allowed",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        )
})
public @interface LogoutOperation {
}