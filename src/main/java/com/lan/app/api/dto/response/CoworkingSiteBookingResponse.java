package com.lan.app.api.dto.response;

import java.util.UUID;

public record CoworkingSiteBookingResponse(
    UUID id,
    String status
) {
}
