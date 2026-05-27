package com.lan.app.infrastructure.baserow.exception;

public abstract class BaserowException extends RuntimeException {

    protected BaserowException(String message) {
        super(message);
    }

    protected BaserowException(String message, Throwable cause) {
        super(message, cause);
    }
}