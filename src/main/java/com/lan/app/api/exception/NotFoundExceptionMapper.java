package com.lan.app.api.exception;

import com.lan.app.api.dto.response.ErrorResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
@ApplicationScoped
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        var body = new ErrorResponse(
                ErrorCode.ROUTE_NOT_FOUND.name(),
                "Requested resource was not found.",
                Map.of()
        );

        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
