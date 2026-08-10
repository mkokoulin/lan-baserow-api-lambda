package com.lan.app.infrastructure.baserow.repository;

import com.baserow.repository.AbstractBaserowRepository;
import com.lan.app.domain.model.DigestSubscriber;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.UpdateDigestSubscriptionRequest;
import com.lan.app.repository.WeeklyDigestRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class BaserowWeeklyDigestRepository extends AbstractBaserowRepository implements WeeklyDigestRepository {

    private final int guestsTableId;
    private final BaserowGuestClient client;

    public BaserowWeeklyDigestRepository(
        @ConfigProperty(name = "baserow.guests.guests-table-id") int guestsTableId,
        @RestClient BaserowGuestClient client
    ) {
        this.guestsTableId = guestsTableId;
        this.client = client;
    }

    @Override
    public List<DigestSubscriber> findSubscribers() {
        return execute(() -> client.listAllRaw(guestsTableId)).results().stream()
            .filter(row -> row.telegramChatId() != null)
            // null/missing digest_subscribed means still subscribed (opt-out default) —
            // only an explicit false opts a guest out.
            .filter(row -> !Boolean.FALSE.equals(row.digestSubscribed()))
            .map(row -> new DigestSubscriber(row.id(), row.telegramChatId()))
            .toList();
    }

    @Override
    public void unsubscribe(int guestRowId) {
        execute(() -> client.patchDigestSubscribed(guestsTableId, guestRowId, new UpdateDigestSubscriptionRequest(false)));
    }

    @Override
    public void subscribe(int guestRowId) {
        execute(() -> client.patchDigestSubscribed(guestsTableId, guestRowId, new UpdateDigestSubscriptionRequest(true)));
    }
}
