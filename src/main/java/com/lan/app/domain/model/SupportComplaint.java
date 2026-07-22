package com.lan.app.domain.model;

public record SupportComplaint(
    String name,
    String phone,
    String telegram,
    String topic,
    String topicCustom,
    String comment
) {}
