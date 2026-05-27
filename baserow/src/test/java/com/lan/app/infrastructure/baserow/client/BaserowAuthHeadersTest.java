package com.lan.app.infrastructure.baserow.client;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.ws.rs.client.ClientRequestContext;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class BaserowAuthHeadersTest {

    @Test
    void filter_setsAuthorizationHeader() throws Exception {
        var filter = new BaserowAuthHeaders();
        setToken(filter, "my-secret-token");

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        var ctx = Mockito.mock(ClientRequestContext.class);
        when(ctx.getHeaders()).thenReturn(headers);

        filter.filter(ctx);

        assertEquals("Token my-secret-token", headers.getFirst("Authorization"));
    }

    @Test
    void filter_replacesExistingAuthorizationHeader() throws Exception {
        var filter = new BaserowAuthHeaders();
        setToken(filter, "new-token");

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("Authorization", "Token old-token");
        var ctx = Mockito.mock(ClientRequestContext.class);
        when(ctx.getHeaders()).thenReturn(headers);

        filter.filter(ctx);

        assertEquals("Token new-token", headers.getFirst("Authorization"));
    }

    private static void setToken(BaserowAuthHeaders filter, String token) throws Exception {
        Field field = BaserowAuthHeaders.class.getDeclaredField("token");
        field.setAccessible(true);
        field.set(filter, token);
    }
}
