package com.lan.app.repository;

import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.service.command.UpdateCoworkingGuestCommand;

import java.util.Optional;
import java.util.UUID;

public interface CoworkingGuestRepository {
    CoworkingGuest get(UUID externalId);
    Optional<CoworkingGuest> findByChatId(Long chatId);
    Optional<CoworkingGuest> findByPhone(String phone);
    Optional<CoworkingGuest> linkChatIdByPhone(String phone, Long chatId);
    CoworkingGuest linkChatIdById(UUID externalId, Long chatId);
    void unlinkChatId(Long chatId);
    CoworkingGuest create(String firstName, String lastName, String phone, String telegram, Long telegramChatId);
    CoworkingGuest update(UUID externalId, UpdateCoworkingGuestCommand patch);
}
