package com.lan.app.infrastructure.baserow.exception;

public class BaserowUnauthorizedException extends BaserowException {

    public BaserowUnauthorizedException() {
        super("Baserow request rejected: invalid or missing token.");
    }
}
