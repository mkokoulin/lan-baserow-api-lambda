package com.lan.app.api.dto.response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "VacancyResponse",
    description = "Job vacancy details published on the careers page"
)
public record VacancyResponse(

    @Schema(
        description = "External unique identifier of the vacancy",
        examples = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "Vacancy title",
        examples = "Вакансия: Бармен",
        required = true
    )
    String title,

    @Schema(
        description = "Free-text application deadline",
        examples = "Актуально до 20 сентября 2024",
        required = true
    )
    String deadline,

    @Schema(
        description = "Full vacancy description",
        required = true
    )
    String description,

    @Schema(
        description = "Link to the application form or job posting",
        nullable = true
    )
    String href
) {
}
