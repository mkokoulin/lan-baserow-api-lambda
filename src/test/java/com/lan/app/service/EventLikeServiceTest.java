package com.lan.app.service;

import java.util.Map;
import java.util.UUID;

import com.lan.app.repository.EventLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventLikeService")
class EventLikeServiceTest {

    static final UUID EVENT_ID = UUID.randomUUID();
    static final String ANON_ID = "anon-123";

    @Mock
    EventLikeRepository repo;

    @Test
    @DisplayName("countsByEvent делегирует в repo")
    void countsByEvent_delegates() {
        var service = new EventLikeService(repo);
        when(repo.countsByEvent()).thenReturn(Map.of(EVENT_ID, 3L));

        assertEquals(Map.of(EVENT_ID, 3L), service.countsByEvent());
    }

    @Test
    @DisplayName("count делегирует в repo")
    void count_delegates() {
        var service = new EventLikeService(repo);
        when(repo.count(EVENT_ID)).thenReturn(5L);

        assertEquals(5L, service.count(EVENT_ID));
    }

    @Test
    @DisplayName("like делегирует в repo и возвращает актуальный count")
    void like_delegates() {
        var service = new EventLikeService(repo);
        when(repo.like(EVENT_ID, ANON_ID)).thenReturn(1L);

        assertEquals(1L, service.like(EVENT_ID, ANON_ID));
    }

    @Test
    @DisplayName("unlike делегирует в repo и возвращает актуальный count")
    void unlike_delegates() {
        var service = new EventLikeService(repo);
        when(repo.unlike(EVENT_ID, ANON_ID)).thenReturn(0L);

        assertEquals(0L, service.unlike(EVENT_ID, ANON_ID));
    }
}
