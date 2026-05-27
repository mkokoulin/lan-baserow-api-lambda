package com.lan.app.infrastructure.baserow.exception;

public class BaserowRateLimitedException extends BaserowException {

    public BaserowRateLimitedException() {
        super("Baserow rate limit exceeded.");
    }
}
