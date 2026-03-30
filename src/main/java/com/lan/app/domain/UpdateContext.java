package com.lan.app.domain;

public record UpdateContext(Long chatId, Long userId, Integer messageId, String messageText, String callbackData,
                            boolean callback) {

    public static UpdateContext fromIncomingUpdate(IncomingUpdate update) {
        return new UpdateContext(
                update.getChatId(),
                update.getUserId(),
                null,
                update.getText(),
                update.getCallbackData(),
                update.getCallbackData() != null && !update.getCallbackData().isBlank()
        );
    }

    // ===== helpers =====

    public boolean hasText() {
        return messageText != null && !messageText.isBlank();
    }

    public boolean hasCallback() {
        return callbackData != null && !callbackData.isBlank();
    }

    public boolean isCommand() {
        return hasText() && messageText.startsWith("/");
    }

    public String command() {
        if (hasCallback() && callbackData.startsWith("/")) {
            return callbackData.substring(1);
        }

        if (isCommand()) {
            return messageText.substring(1).split("\\s+")[0];
        }

        return null;
    }

    public String commandArgs() {
        if (!isCommand()) return null;

        String[] parts = messageText.split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }
}
