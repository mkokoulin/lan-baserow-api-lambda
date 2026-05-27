package com.lan.app.infrastructure.baserow.exception;

import java.util.UUID;

public class BaserowDataIntegrityException extends BaserowException {

    public BaserowDataIntegrityException(String entityName, UUID externalId) {
        super(entityName + " data is incomplete in Baserow. externalId=" + externalId);
    }
}
