package com.lan.app.service.command;

public record UpdateCoworkingGuestCommand(
    String firstName,
    String lastName,
    String phone,
    String telegram
) {
}
