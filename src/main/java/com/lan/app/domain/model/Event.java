package com.lan.app.domain.model;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;

public record Event(
    Id id,
    String name,
    Instant dateStart,
    Instant dateEnd,
    String description,
    URI externalRegistrationUrl,
    URI registrationUrl,
    URI instagramUrl,
    URI telegramUrl,
    boolean showForm,
    List<String> notifications,
    String comment,
    Integer position,
    boolean showOnHome,
    boolean isVisible,
    boolean requiresPrepayment,
    BigDecimal price,
    String imageUrl,
    Integer maxCapacity,
    boolean soldOut
) {}
