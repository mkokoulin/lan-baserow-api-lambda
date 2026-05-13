package com.lan.app.api.resource;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RegistrationConfirmStore {

    private final Set<String> confirmed = ConcurrentHashMap.newKeySet();

    public void confirm(String regId) {
        confirmed.add(regId);
    }

    public boolean isConfirmed(String regId) {
        return confirmed.contains(regId);
    }
}
