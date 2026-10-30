package com.workzeno.workzeno.shared.openapi.schema;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiErrorResponse")
public record ApiErrorResponse(

        String type,

        String title,

        Integer status,

        String detail,

        String instance,

        String requestId
) {
}