package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Map;

@Schema(
    name = "ErrorResponse",
    description = "Standard error response returned by the API when a request fails. " +
        "Contains a machine-readable error code, a human-readable message, and optional context details."
)
public record ErrorResponse(

    @Schema(
        description = "Machine-readable error code identifying the failure type. " +
            "Typically a stable, uppercase string that clients can branch on.",
        examples = "VALIDATION_FAILED",
        required = true
    )
    String code,

    @Schema(
        description = "Human-readable error message describing what went wrong. " +
            "Intended for display to the end user or for logging.",
        examples = "One or more fields failed validation",
        required = true
    )
    String message,

    @Schema(
        description = "Optional map with additional context about the error. " +
            "Structure depends on the error code — for validation errors it may contain field-level violations, " +
            "for not-found errors it may contain the requested identifier, and so on.",
        examples = "{\"field\": \"phone\", \"reason\": \"must not be blank\"}",
        nullable = true
    )
    Map<String, Object> details
) {
}