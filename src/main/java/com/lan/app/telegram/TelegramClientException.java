package com.lan.app.telegram;

public class TelegramClientException extends RuntimeException {
    public TelegramClientException(String message) {
        super(message);
    }
    
    public TelegramClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
