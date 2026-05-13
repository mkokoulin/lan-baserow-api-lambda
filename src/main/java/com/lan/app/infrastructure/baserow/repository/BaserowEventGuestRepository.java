package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.EventGuest;
import com.lan.app.infrastructure.baserow.client.BaserowEventGuestClient;
import com.lan.app.infrastructure.baserow.dto.CreateEventGuestRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateGuestTelegramChatIdRequest;
import com.lan.app.infrastructure.baserow.mapper.BaserowEventGuestMapper;
import com.lan.app.repository.EventGuestRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BaserowEventGuestRepository extends AbstractBaserowRepository implements EventGuestRepository {

    private static final Logger log = Logger.getLogger(BaserowEventGuestRepository.class);

    private final int guestsTableId;

    private final BaserowEventGuestClient client;
    private final BaserowEventGuestMapper mapper;

    @Inject
    public BaserowEventGuestRepository(
        @ConfigProperty(name = "baserow.events.guests-table-id") int guestsTableId,
        @RestClient BaserowEventGuestClient client,
        BaserowEventGuestMapper mapper
    ) {
        this.guestsTableId = guestsTableId;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public EventGuest get(UUID externalId) {
        var row = client.findUniqueByExternalId(guestsTableId, externalId);
        return mapper.toDomain(row);
    }

    @Override
    public EventGuest create(String firstName, String lastName, String phone, String telegram, String source) {
        var body = new CreateEventGuestRowRequest(firstName, lastName, phone, telegram, source);
        var created = execute(() -> client.create(guestsTableId, body));
        return mapper.toDomain(created);
    }

    @Override
    public Optional<EventGuest> findByTelegramChatId(Long chatId) {
        var response = execute(() -> client.findByChatIdRaw(guestsTableId, chatId));
        if (response.results().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDomain(response.results().getFirst()));
    }

    @Override
    public void storeTelegramChatId(int guestRowId, Long chatId) {
        log.infof("PATCH telegram_chat_id=%d for guestRowId=%d tableId=%d", chatId, guestRowId, guestsTableId);
        try {
            execute(() -> client.patchTelegramChatId(
                guestsTableId,
                guestRowId,
                new UpdateGuestTelegramChatIdRequest(chatId)
            ));
            log.infof("PATCH telegram_chat_id OK for guestRowId=%d", guestRowId);
        } catch (Exception e) {
            log.errorf(e, "PATCH telegram_chat_id FAILED for guestRowId=%d: %s", guestRowId, e.getMessage());
            throw e;
        }
    }
}
