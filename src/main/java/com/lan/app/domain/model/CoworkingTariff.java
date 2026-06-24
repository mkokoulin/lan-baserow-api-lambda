package com.lan.app.domain.model;

import java.util.UUID;

public record CoworkingTariff(
    UUID id,
    String name,
    Integer price,
    String meetingRoom,
    boolean fixedDesk,
    boolean filterCoffeeAndTea,
    boolean printoutScan,
    boolean luggageStorage,
    CoworkingTariffType type
) {}
