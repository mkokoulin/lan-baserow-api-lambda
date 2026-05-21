package com.lan.app.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "CreateReviewRequest",
    description = "Payload for submitting a new review"
)
public record CreateReviewRequest(

    @Schema(
        description = "Name of the review author",
        examples = "Ivan Petrov",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank String authorName,

    @Schema(
        description = "Rating from 1 (worst) to 5 (best)",
        examples = "5",
        required = true,
        minimum = "1",
        maximum = "5"
    )
    @NotNull @Min(1) @Max(5) Integer rating,

    @Schema(
        description = "Review text",
        examples = "Great coworking space, very comfortable!"
    )
    String text
) {
}
