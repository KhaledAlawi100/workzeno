package com.workzeno.workzeno.shared.openapi;

import com.workzeno.workzeno.projects.dto.ProjectResponse;
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
        summary = "Create project",
        description = "Creates a new " +
                "project owned by the authenticated user."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Project created successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProjectResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid project data",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Authentication required",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiErrorResponse.class)
                )
        )
})
public @interface CreateProjectOperation {
}