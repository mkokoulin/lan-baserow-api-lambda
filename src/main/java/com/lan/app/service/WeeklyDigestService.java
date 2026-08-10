package com.lan.app.service;

import com.lan.app.domain.model.DigestSubscriber;
import com.lan.app.repository.WeeklyDigestRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class WeeklyDigestService {

    private final WeeklyDigestRepository repo;
    private final EventGuestService guestService;

    public WeeklyDigestService(WeeklyDigestRepository repo, EventGuestService guestService) {
        this.repo = repo;
        this.guestService = guestService;
    }

    public List<DigestSubscriber> findSubscribers() {
        return repo.findSubscribers();
    }

    public void unsubscribe(int guestRowId) {
        repo.unsubscribe(guestRowId);
    }

    // Resolves (or creates) the guest behind chatId/phone — same upsert semantics as event
    // registration — then opts them into the digest. Lets the bot offer a one-tap "subscribe"
    // button without needing to already know the guest's Baserow row id.
    public void subscribe(String firstName, String lastName, String phone, String telegram, String source, Long chatId) {
        var guest = guestService.create(firstName, lastName, phone, telegram, source, chatId);
        repo.subscribe(guest.id().internalId());
    }
}
