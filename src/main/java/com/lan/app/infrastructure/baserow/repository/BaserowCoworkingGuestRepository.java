package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingGuestClient;
import com.lan.app.infrastructure.baserow.dto.CreateCoworkingGuestRowRequest;
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

    private final int coworkingGuestsTableId;

    private final BaserowCoworkingGuestClient client;
    private final BaserowCoworkingGuestMapper mapper;

    @Inject
    public BaserowCoworkingGuestRepository(
        @ConfigProperty(name = "baserow.coworking.guests-table-id") int coworkingGuestsTableId,
        @RestClient BaserowCoworkingGuestClient client,
        BaserowCoworkingGuestMapper mapper
    ) {
        this.coworkingGuestsTableId = coworkingGuestsTableId;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public CoworkingGuest get(UUID externalId) {
        var created = client.findUniqueByExternalId(coworkingGuestsTableId, externalId);

        return mapper.toDomain(created);
    }

    @Override
    public Optional<CoworkingGuest> findByChatId(Long chatId) {
        var resp = client.findByChatIdRaw(coworkingGuestsTableId, chatId);
        if (resp.count() == 0 || resp.results().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(resp.results().getFirst()));
    }

    @Override
    public Optional<CoworkingGuest> linkChatIdByPhone(String phone, Long chatId) {
        var resp = client.findByPhoneRaw(coworkingGuestsTableId, phone);
        if (resp.count() == 0 || resp.results().isEmpty()) {
            return Optional.empty();
        }
        var row = resp.results().getFirst();
        var updated = client.patchChatId(coworkingGuestsTableId, row.id(), new LinkChatIdRowRequest(chatId));
        return Optional.of(mapper.toDomain(updated));
    }

    @Override
    public CoworkingGuest create(String firstName, String lastName, String phone, String telegram) {
        var body = new CreateCoworkingGuestRowRequest(firstName, lastName, phone, telegram);

        var created = client.create(coworkingGuestsTableId, body);
        return mapper.toDomain(created);
    }

    @Override
    public CoworkingGuest update(UUID externalId, UpdateCoworkingGuestCommand cmd) {
        var existing = client.findUniqueByExternalId(coworkingGuestsTableId, externalId);

        var patch = mapper.toBaserowPatch(cmd);
        var updated = client.update(coworkingGuestsTableId, existing.id(), patch);

        return mapper.toDomain(updated);
    }
}