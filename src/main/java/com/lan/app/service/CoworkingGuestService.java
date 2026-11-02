package com.lan.app.service;

import com.lan.app.domain.PhoneNumbers;
import com.lan.app.domain.exception.BusinessConflictException;
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

    public Optional<CoworkingGuest> findByPhone(String phone) {
        return repo.findByPhone(PhoneNumbers.normalize(phone));
    }

    public CoworkingGuest linkChatIdById(UUID externalId, Long chatId) {
        return repo.linkChatIdById(externalId, chatId);
    }

    public Optional<CoworkingGuest> linkChatIdByPhone(String phone, Long chatId) {
        return repo.linkChatIdByPhone(PhoneNumbers.normalize(phone), chatId);
    }

    public void unlinkChat(Long chatId) {
        repo.unlinkChatId(chatId);
    }

    public CoworkingGuest create(String firstName, String lastName, String phone, String telegram, Long telegramChatId) {
        String normalizedPhone = PhoneNumbers.normalize(phone);
        if (repo.findByPhone(normalizedPhone).isPresent()) {
            throw new BusinessConflictException(
                "Guest with phone " + normalizedPhone + " already exists.",
                java.util.Map.of("phone", normalizedPhone)
            );
        }
        if (telegramChatId != null && repo.findByChatId(telegramChatId).isPresent()) {
            throw new BusinessConflictException(
                "Guest with Telegram chat ID " + telegramChatId + " already exists.",
                java.util.Map.of("telegramChatId", telegramChatId)
            );
        }
        return repo.create(firstName, lastName, normalizedPhone, telegram, telegramChatId);
    }

    public CoworkingGuest update(UUID externalId, UpdateCoworkingGuestCommand cmd) {
        if (cmd != null && cmd.phone() != null) {
            cmd = new UpdateCoworkingGuestCommand(cmd.firstName(), cmd.lastName(), PhoneNumbers.normalize(cmd.phone()), cmd.telegram());
        }
        return repo.update(externalId, cmd);
    }
}
