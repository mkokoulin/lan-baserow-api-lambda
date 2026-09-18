package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.SupportComplaint;
import com.lan.app.infrastructure.baserow.dto.BaserowSupportComplaintRow;
import com.lan.app.infrastructure.baserow.dto.CreateSupportComplaintRowRequest;
import com.lan.app.service.command.CreateSupportComplaintCommand;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.Map;

@ApplicationScoped
public class BaserowSupportComplaintMapper {

    private static final Logger log = Logger.getLogger(BaserowSupportComplaintMapper.class);

    // Baserow rejects (400) any value for this single-select field that isn't one of its
    // existing options, so an unrecognized topic code must never be forwarded as-is.
    private static final String FALLBACK_TOPIC_LABEL = "Другое";

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
        String topicLabel = TOPIC_LABELS.get(cmd.topic());
        if (topicLabel == null) {
            log.warnf("Unrecognized complaint topic code '%s' — falling back to '%s' instead of forwarding it to Baserow's select field", cmd.topic(), FALLBACK_TOPIC_LABEL);
            topicLabel = FALLBACK_TOPIC_LABEL;
        }
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
