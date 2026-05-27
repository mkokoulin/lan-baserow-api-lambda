package com.lan.app.infrastructure.baserow.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class BaserowAuthHeaders implements ClientRequestFilter {

    @ConfigProperty(name = "baserow.token")
    String token;

    @Override
    public void filter(ClientRequestContext requestContext) {
        requestContext.getHeaders().putSingle("Authorization", "Token " + token);
    }
}
