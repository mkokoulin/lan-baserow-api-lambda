package com.lan.app.domain.model;

import java.util.UUID;

public record EventCoworkingTariff(
    UUID id,
    String name,
    Integer price,
    String meetingRoom,
    boolean fixedDesk,
    boolean filterCoffeeAndTea,
    boolean printoutScan,
    boolean luggageStorage
) {
}
