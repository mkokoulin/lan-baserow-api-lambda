package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.service.command.CreateSupportComplaintCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("BaserowSupportComplaintMapper")
class BaserowSupportComplaintMapperTest {

    final BaserowSupportComplaintMapper mapper = new BaserowSupportComplaintMapper();

    @Test
    @DisplayName("известный topic код → маппится на русский лейбл Baserow")
    void knownTopic_mapsToRussianLabel() {
        var cmd = new CreateSupportComplaintCommand("Ivan", "+79161234567", "ivan", "billing", null, "comment");

        var req = mapper.toBaserowRequest(cmd);

        assertEquals("Оплата и биллинг", req.topic());
        assertEquals("Новая", req.status());
    }

    @Test
    @DisplayName("нераспознанный topic код → не пересылается в Baserow как есть, используется 'Другое'")
    void unknownTopic_fallsBackInsteadOfForwardingRawValue() {
        var cmd = new CreateSupportComplaintCommand("Ivan", "+79161234567", "ivan", "not-a-real-topic", null, "comment");

        var req = mapper.toBaserowRequest(cmd);

        assertEquals("Другое", req.topic());
    }
}
