package com.lan.app.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory tracking of each event's last-known sold-out state, so the freed-capacity
 * admin alert can detect a sold-out -> has-room transition instead of re-alerting on
 * every poll. Same accepted limitation as RegistrationConfirmStore/RegistrationPaidStore:
 * state resets on cold start.
 */
@ApplicationScoped
public class EventCapacityAlertStore {

    private final Map<Integer, Boolean> lastKnownSoldOut = new ConcurrentHashMap<>();
    private final Set<Integer> alerted = ConcurrentHashMap.newKeySet();

    public Boolean getLastKnown(int eventRowId) {
        return lastKnownSoldOut.get(eventRowId);
    }

    public void recordState(int eventRowId, boolean soldOut) {
        lastKnownSoldOut.put(eventRowId, soldOut);
        if (soldOut) {
            alerted.remove(eventRowId);
        }
    }

    public boolean alreadyAlerted(int eventRowId) {
        return alerted.contains(eventRowId);
    }

    public void markAlerted(int eventRowId) {
        alerted.add(eventRowId);
    }
}
