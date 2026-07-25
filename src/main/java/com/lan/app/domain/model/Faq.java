package com.lan.app.domain.model;

public record Faq(
    Id id,
    String questionEn,
    String questionRu,
    String answerEn,
    String answerRu,
    Integer position
) {}
