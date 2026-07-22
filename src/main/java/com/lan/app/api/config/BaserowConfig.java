package com.lan.app.api.config;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;

@ConfigMapping(prefix = "baserow")
public interface BaserowConfig {

    @NotBlank
    String token();

    Guests guests();

    Events events();

    Coworking coworking();

    Support support();

    Careers careers();

    interface Guests {
        int guestsTableId();
    }

    interface Events {
        int eventsTableId();
        int registrationsTableId();
        int notificationsTableId();
        int eventNotificationsTableId();
        int festivalsTableId();
        int paymentsTableId();
        int notificationResultsTableId();
    }

    interface Coworking {
        int notificationsTableId();
        int tariffsTableId();
        int activeTariffsTableId();
        int meetingRoomBookingsTableId();
        int guestTariffsTableId();
        int newsTableId();
        int reviewsTableId();
        int siteBookingsTableId();
    }

    interface Support {
        int complaintsTableId();
    }

    interface Careers {
        int vacanciesTableId();
    }
}