package com.lan.app.domain.model;

//import java.util.UUID;

public record EventRegistration(
    Id id,
//    UUID externalId,
    Id eventId,
    Id guestId,
    Integer guestCount,
    String comment,
    String source,
    boolean isPaid
) {
}
