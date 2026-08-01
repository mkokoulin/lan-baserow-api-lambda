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
        // The site doesn't know the guest's chatId until they confirm via the bot's deep link,
        // so a phone match is often the only way to catch a returning guest at creation time —
        // this is what actually stops a second site registration from ever creating a duplicate.
        if (phone != null && !phone.isBlank()) {
            var existing = repo.findByPhone(phone);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        return repo.create(firstName, lastName, phone, telegram, source, chatId);
    }
}
