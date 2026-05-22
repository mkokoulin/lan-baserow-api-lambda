package com.lan.app.api.resource;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RegistrationPaidStore {
    private final Set<String> paid = ConcurrentHashMap.newKeySet();

    public void markPaid(String regId) {
        paid.add(regId);
    }

    public boolean isPaid(String regId) {
        return paid.contains(regId);
    }
}
