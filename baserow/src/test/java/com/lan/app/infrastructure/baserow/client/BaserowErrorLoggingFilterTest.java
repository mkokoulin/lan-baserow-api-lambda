package com.lan.app.infrastructure.baserow.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BaserowErrorLoggingFilterTest {

    private final BaserowErrorLoggingFilter filter = new BaserowErrorLoggingFilter();

    @Test
    void filter_doesNothingForSuccessResponse() throws IOException {
        var req = mockRequest();
        var res = mockResponse(200, null);

        assertDoesNotThrow(() -> filter.filter(req, res));
        verify(res, never()).setEntityStream(any());
    }

    @Test
    void filter_doesNothingFor3xxResponse() throws IOException {
        var req = mockRequest();
        var res = mockResponse(302, null);

        assertDoesNotThrow(() -> filter.filter(req, res));
        verify(res, never()).setEntityStream(any());
    }

    @Test
    void filter_logsAndRewindsStreamFor4xxResponse() throws IOException {
        var body = "{\"error\":\"ERROR_ROW_DOES_NOT_EXIST\"}";
        var req = mockRequest();
        var res = mockResponse(404, body);

        filter.filter(req, res);

        // stream must be rewound so downstream can still read it
        verify(res).setEntityStream(any());
    }

    @Test
    void filter_logsAndRewindsStreamFor5xxResponse() throws IOException {
        var body = "{\"error\":\"INTERNAL_SERVER_ERROR\"}";
        var req = mockRequest();
        var res = mockResponse(500, body);

        filter.filter(req, res);

        verify(res).setEntityStream(any());
    }

    @Test
    void filter_handlesEmptyBodyWithoutException() throws IOException {
        var req = mockRequest();
        var res = mockResponse(400, null);

        assertDoesNotThrow(() -> filter.filter(req, res));
    }

    private static ClientRequestContext mockRequest() throws IOException {
        var ctx = mock(ClientRequestContext.class);
        when(ctx.getMethod()).thenReturn("GET");
        when(ctx.getUri()).thenReturn(URI.create("https://api.baserow.io/api/database/rows/table/1/99/"));
        return ctx;
    }

    private static ClientResponseContext mockResponse(int status, String body) throws IOException {
        var ctx = mock(ClientResponseContext.class);
        when(ctx.getStatus()).thenReturn(status);
        if (body != null) {
            when(ctx.hasEntity()).thenReturn(true);
            when(ctx.getEntityStream())
                .thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        } else {
            when(ctx.hasEntity()).thenReturn(false);
        }
        return ctx;
    }
}
