package com.lan.app.repository;

import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.service.command.UpdateCoworkingGuestCommand;

import java.util.Optional;
import java.util.UUID;

public interface CoworkingGuestRepository {
    CoworkingGuest get(UUID externalId);
    Optional<CoworkingGuest> findByChatId(Long chatId);
    Optional<CoworkingGuest> linkChatIdByPhone(String phone, Long chatId);
    CoworkingGuest create(String firstName, String lastName, String phone, String telegram);
    CoworkingGuest update(UUID externalId, UpdateCoworkingGuestCommand patch);
}
