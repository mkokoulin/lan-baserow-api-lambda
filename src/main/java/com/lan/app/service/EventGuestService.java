package com.lan.app.service;

import com.lan.app.domain.model.EventGuest;
import com.lan.app.repository.EventGuestRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class EventGuestService {

    EventGuestRepository repo;

    public EventGuestService(EventGuestRepository repo) {
        this.repo = repo;
    }

    public EventGuest get(UUID externalId) {
        return repo.get(externalId);
    }

    public EventGuest create(String firstName, String lastName, String phone, String telegram, String source, Long chatId) {
        if (chatId != null) {
            var existing = repo.findByTelegramChatId(chatId);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        return repo.create(firstName, lastName, phone, telegram, source, chatId);
    }
}
