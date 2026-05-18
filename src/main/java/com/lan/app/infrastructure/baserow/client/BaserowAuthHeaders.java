package com.lan.app.infrastructure.baserow.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import com.lan.app.api.config.BaserowConfig;

@ApplicationScoped
public class BaserowAuthHeaders implements ClientHeadersFactory {

    private final BaserowConfig config;

    public BaserowAuthHeaders(BaserowConfig config) {
        this.config = config;
    }

    @Override
    public MultivaluedMap<String, String> update(
        MultivaluedMap<String, String> incoming,
        MultivaluedMap<String, String> outgoing
    ) {
        outgoing.putSingle("Authorization", "Bearer " + config.token());
        return outgoing;
    }
}
