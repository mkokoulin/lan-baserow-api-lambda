package com.lan.app.service.command;

public record CreateBirthdayWishCommand(
    String authorName,
    String telegramUserId,
    String text
) {
}
