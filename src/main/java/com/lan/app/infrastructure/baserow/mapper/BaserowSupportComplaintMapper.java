package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.SupportComplaint;
import com.lan.app.infrastructure.baserow.dto.BaserowSupportComplaintRow;
import com.lan.app.infrastructure.baserow.dto.CreateSupportComplaintRowRequest;
import com.lan.app.service.command.CreateSupportComplaintCommand;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class BaserowSupportComplaintMapper {

    // Maps topic code (from the website) to the Russian label used as the
    // Baserow single-select option value.
    private static final Map<String, String> TOPIC_LABELS = Map.of(
        "wifi",        "Wi-Fi и интернет",
        "equipment",   "Оборудование и техника",
        "cleanliness", "Чистота и порядок",
        "staff",       "Персонал и сервис",
        "billing",     "Оплата и биллинг",
        "events",      "Мероприятия и регистрация",
        "other",       "Другое"
    );

    public SupportComplaint toDomain(BaserowSupportComplaintRow row, String topic) {
        return new SupportComplaint(
            row.name(),
            row.phone(),
            row.telegram(),
            topic,
            row.topicCustom(),
            row.comment()
        );
    }

    public CreateSupportComplaintRowRequest toBaserowRequest(CreateSupportComplaintCommand cmd) {
        String topicLabel = TOPIC_LABELS.getOrDefault(cmd.topic(), cmd.topic());
        return new CreateSupportComplaintRowRequest(
            cmd.name(),
            cmd.phone(),
            cmd.telegram(),
            topicLabel,
            cmd.topicCustom(),
            cmd.comment(),
            "Новая"
        );
    }
}
