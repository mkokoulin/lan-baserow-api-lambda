package com.lan.app.infrastructure.baserow.repository;
import com.baserow.repository.AbstractBaserowRepository;

import com.lan.app.domain.model.EventGuest;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.CreateGuestRowRequest;
import com.lan.app.infrastructure.baserow.dto.LinkChatIdRowRequest;
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

    private final BaserowGuestClient client;
    private final BaserowEventGuestMapper mapper;

    @Inject
    public BaserowEventGuestRepository(
        @ConfigProperty(name = "baserow.guests.guests-table-id") int guestsTableId,
        @RestClient BaserowGuestClient client,
        BaserowEventGuestMapper mapper
    ) {
        this.guestsTableId = guestsTableId;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public EventGuest get(UUID externalId) {
        return mapper.toDomain(client.findUniqueByExternalId(guestsTableId, externalId));
    }

    @Override
    public EventGuest create(String firstName, String lastName, String phone, String telegram, String source, Long chatId) {
        var body = new CreateGuestRowRequest(firstName, lastName, phone, telegram, source, chatId);
        return mapper.toDomain(execute(() -> client.create(guestsTableId, body)));
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
            execute(() -> client.patchChatId(guestsTableId, guestRowId, new LinkChatIdRowRequest(chatId)));
            log.infof("PATCH telegram_chat_id OK for guestRowId=%d", guestRowId);
        } catch (Exception e) {
            log.errorf(e, "PATCH telegram_chat_id FAILED for guestRowId=%d: %s", guestRowId, e.getMessage());
            throw e;
        }
    }
}
