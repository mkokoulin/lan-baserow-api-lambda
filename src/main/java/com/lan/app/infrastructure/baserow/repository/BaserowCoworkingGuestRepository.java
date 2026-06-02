package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.CreateGuestRowRequest;
import com.lan.app.infrastructure.baserow.dto.LinkChatIdRowRequest;
import com.lan.app.infrastructure.baserow.mapper.BaserowCoworkingGuestMapper;
import com.lan.app.repository.CoworkingGuestRepository;
import com.lan.app.service.command.UpdateCoworkingGuestCommand;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BaserowCoworkingGuestRepository implements CoworkingGuestRepository {

    private final int guestsTableId;

    private final BaserowGuestClient client;
    private final BaserowCoworkingGuestMapper mapper;

    @Inject
    public BaserowCoworkingGuestRepository(
        @ConfigProperty(name = "baserow.guests.guests-table-id") int guestsTableId,
        @RestClient BaserowGuestClient client,
        BaserowCoworkingGuestMapper mapper
    ) {
        this.guestsTableId = guestsTableId;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public CoworkingGuest get(UUID externalId) {
        return mapper.toDomain(client.findUniqueByExternalId(guestsTableId, externalId));
    }

    @Override
    public Optional<CoworkingGuest> findByChatId(Long chatId) {
        var resp = client.findByChatIdRaw(guestsTableId, chatId);
        if (resp.count() == 0 || resp.results().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(resp.results().getFirst()));
    }

    @Override
    public Optional<CoworkingGuest> findByPhone(String phone) {
        var resp = client.findByPhoneRaw(guestsTableId, phone);
        if (resp.count() == 0 || resp.results().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(resp.results().getFirst()));
    }

    @Override
    public CoworkingGuest linkChatIdById(UUID externalId, Long chatId) {
        var row = client.findUniqueByExternalId(guestsTableId, externalId);
        var updated = client.patchChatId(guestsTableId, row.id(), new LinkChatIdRowRequest(chatId));
        return mapper.toDomain(updated);
    }

    @Override
    public Optional<CoworkingGuest> linkChatIdByPhone(String phone, Long chatId) {
        var resp = client.findByPhoneRaw(guestsTableId, phone);
        if (resp.count() == 0 || resp.results().isEmpty()) {
            return Optional.empty();
        }
        var row = resp.results().getFirst();
        var updated = client.patchChatId(guestsTableId, row.id(), new LinkChatIdRowRequest(chatId));
        return Optional.of(mapper.toDomain(updated));
    }

    @Override
    public void unlinkChatId(Long chatId) {
        var resp = client.findByChatIdRaw(guestsTableId, chatId);
        if (resp.count() == 0 || resp.results().isEmpty()) {
            return;
        }
        client.patchChatId(guestsTableId, resp.results().getFirst().id(), new LinkChatIdRowRequest(null));
    }

    @Override
    public CoworkingGuest create(String firstName, String lastName, String phone, String telegram, Long telegramChatId) {
        var body = new CreateGuestRowRequest(firstName, lastName, phone, telegram, null, telegramChatId);
        return mapper.toDomain(client.create(guestsTableId, body));
    }

    @Override
    public CoworkingGuest update(UUID externalId, UpdateCoworkingGuestCommand cmd) {
        var existing = client.findUniqueByExternalId(guestsTableId, externalId);
        var patch = mapper.toBaserowPatch(cmd);
        return mapper.toDomain(client.update(guestsTableId, existing.id(), patch));
    }
}
