package com.lan.app.infrastructure.baserow.exception;

import java.util.UUID;

public class BaserowNotFoundException extends BaserowException {

    private final String entityName;
    private final UUID externalId;

    public BaserowNotFoundException(String entityName, UUID externalId) {
        super(entityName + " not found in Baserow.");
        this.entityName = entityName;
        this.externalId = externalId;
    }

    public String entityName() { return entityName; }
    public UUID externalId() { return externalId; }
}