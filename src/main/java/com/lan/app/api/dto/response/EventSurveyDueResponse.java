package com.lan.app.api.dto.response;

public record EventSurveyDueResponse(int eventRowId, String eventName, int guestRowId, int registrationRowId, Long chatId) {}
