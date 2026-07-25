package com.lan.app.api.dto.response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "CoworkingNewResponse",
    description = "Blog post / news item published on the blog page"
)
public record CoworkingNewResponse(
    UUID id,
    String titleEn,
    String titleRu,

    @Schema(
        description = "Full post body (English). May contain sanitized HTML markup " +
            "(e.g. <p>, <strong>, <a>, <ul>/<li>) produced by Baserow's rich text editor; " +
            "clients must sanitize before rendering."
    )
    String bodyEn,

    @Schema(
        description = "Full post body (Russian). May contain sanitized HTML markup " +
            "(e.g. <p>, <strong>, <a>, <ul>/<li>) produced by Baserow's rich text editor; " +
            "clients must sanitize before rendering."
    )
    String bodyRu,

    String imageUrl,
    String link
) {
}
