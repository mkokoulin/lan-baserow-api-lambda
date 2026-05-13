package com.lan.app.api.resource;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RegistrationConfirmStore {

    private final Set<String> confirmed = ConcurrentHashMap.newKeySet();

    // Populated at registration creation time so confirm doesn't need a Baserow lookup.
    private final Map<String, Integer> regToGuestRowId = new ConcurrentHashMap<>();

    public void confirm(String regId) {
        confirmed.add(regId);
    }

    public boolean isConfirmed(String regId) {
        return confirmed.contains(regId);
    }

    public void storeGuestRowId(String regId, int guestRowId) {
        regToGuestRowId.put(regId, guestRowId);
    }

    public Optional<Integer> getGuestRowId(String regId) {
        return Optional.ofNullable(regToGuestRowId.get(regId));
    }
}
