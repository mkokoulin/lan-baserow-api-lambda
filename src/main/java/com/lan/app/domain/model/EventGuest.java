package com.lan.app.domain.model;

public record EventGuest(
    Id id,
    String firstName,
    String lastName,
    String telegram,
    String phone,
    String source,
    Long chatId
) {
}
