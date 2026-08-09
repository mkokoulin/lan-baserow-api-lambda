package com.lan.app.domain.model;

public record EventSurveyDue(int eventRowId, String eventName, int guestRowId, int registrationRowId, Long chatId) {}
