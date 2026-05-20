package com.lan.app.api.config;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;

@ConfigMapping(prefix = "baserow")
public interface BaserowConfig {

    @NotBlank
    String token();

    Events events();

    Coworking coworking();

    interface Events {
        int eventsTableId();
        int registrationsTableId();
        int guestsTableId();
        int notificationsTableId();
        int festivalsTableId();
    }

    interface Coworking {
        int guestsTableId();
        int notificationsTableId();
        int tariffsTableId();
        int activeTariffsTableId();
        int meetingRoomBookingsTableId();
        int guestTariffsTableId();
    }
}