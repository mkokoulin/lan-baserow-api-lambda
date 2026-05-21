package com.lan.app.service;

import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.repository.CoworkingGuestRepository;
import com.lan.app.service.command.UpdateCoworkingGuestCommand;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CoworkingGuestService {

    CoworkingGuestRepository repo;

    public CoworkingGuestService(CoworkingGuestRepository repo) {
        this.repo = repo;
    }

    public CoworkingGuest get(UUID externalId) {
        return repo.get(externalId);
    }

    public Optional<CoworkingGuest> findByChatId(Long chatId) {
        return repo.findByChatId(chatId);
    }

    public Optional<CoworkingGuest> linkChatIdByPhone(String phone, Long chatId) {
        return repo.linkChatIdByPhone(phone, chatId);
    }

    public CoworkingGuest create(String firstName, String lastName, String phone, String telegram) {
        return repo.create(firstName, lastName, phone, telegram);
    }

    public CoworkingGuest update(UUID externalId, UpdateCoworkingGuestCommand cmd) {
        return repo.update(externalId, cmd);
    }
}
