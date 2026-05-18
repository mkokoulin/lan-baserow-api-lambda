package com.lan.app.infrastructure.baserow.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import com.lan.app.api.config.BaserowConfig;

@ApplicationScoped
public class BaserowAuthHeaders implements ClientRequestFilter {

    private final BaserowConfig config;

    public BaserowAuthHeaders(BaserowConfig config) {
        this.config = config;
    }

    @Override
    public void filter(ClientRequestContext requestContext) {
        requestContext.getHeaders().putSingle("Authorization", "Token " + config.token());
    }
}
