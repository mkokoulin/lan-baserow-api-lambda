package com.lan.app.infrastructure.baserow.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class BaserowErrorLoggingFilter implements ClientResponseFilter {

    private static final Logger LOG = Logger.getLogger(BaserowErrorLoggingFilter.class);

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        int status = responseContext.getStatus();
        if (status >= 400) {
            String method = requestContext.getMethod();
            String uri = requestContext.getUri().toString();
            String body = "";
            if (responseContext.hasEntity()) {
                byte[] bytes = responseContext.getEntityStream().readAllBytes();
                body = new String(bytes, StandardCharsets.UTF_8);
                responseContext.setEntityStream(new java.io.ByteArrayInputStream(bytes));
            }
            LOG.errorf("Baserow %d on %s %s — response body: %s", status, method, uri, body);
        }
    }
}
