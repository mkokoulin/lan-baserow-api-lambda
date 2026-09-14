package com.lan.app.service;

import com.lan.app.domain.model.EventGuest;
import com.lan.app.repository.EventGuestRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;
import java.util.regex.Pattern;

@ApplicationScoped
public class EventGuestService {

    // Matches 6+ identical digits in a row, e.g. the "374" + all-zeros placeholder guests type in
    // when they're registering on someone else's behalf and don't have a real number to enter.
    // Real phone numbers don't contain a run this long, so we don't trust these for guest matching —
    // otherwise every guest who ever typed the same placeholder gets silently merged into whichever
    // one of them registered first, with their name/telegram overwriting everyone else's.
    private static final Pattern PLACEHOLDER_DIGIT_RUN = Pattern.compile("(\\d)\\1{5,}");

    EventGuestRepository repo;

    public EventGuestService(EventGuestRepository repo) {
        this.repo = repo;
    }

    public EventGuest get(UUID externalId) {
        return repo.get(externalId);
    }

    public EventGuest create(String firstName, String lastName, String phone, String telegram, String source, Long chatId) {
        if (chatId != null) {
            var existing = repo.findByTelegramChatId(chatId);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        // The site doesn't know the guest's chatId until they confirm via the bot's deep link,
        // so a phone match is often the only way to catch a returning guest at creation time —
        // this is what actually stops a second site registration from ever creating a duplicate.
        if (phone != null && !phone.isBlank() && !isPlaceholderPhone(phone)) {
            var existing = repo.findByPhone(phone);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        return repo.create(firstName, lastName, phone, telegram, source, chatId);
    }

    private static boolean isPlaceholderPhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        return PLACEHOLDER_DIGIT_RUN.matcher(digits).find();
    }
}
