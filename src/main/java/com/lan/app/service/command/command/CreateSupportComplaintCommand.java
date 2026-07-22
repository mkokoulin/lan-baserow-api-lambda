package com.lan.app.service.command;

public record CreateSupportComplaintCommand(
    String name,
    String phone,
    String telegram,
    String topic,
    String topicCustom,
    String comment
) {
}
