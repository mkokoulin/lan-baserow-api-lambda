package com.lan.app.service.command;

public record CreateReviewCommand(
    String authorName,
    Integer rating,
    String text
) {
}
