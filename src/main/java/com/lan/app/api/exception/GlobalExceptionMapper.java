package com.lan.app.api.exception;

import com.lan.app.api.dto.response.ErrorResponse;
import com.lan.app.domain.exception.AppException;
import com.lan.app.domain.exception.BusinessConflictException;
import com.lan.app.domain.exception.ResourceNotFoundException;
import com.lan.app.infrastructure.baserow.exception.BaserowDataIntegrityException;
import com.lan.app.infrastructure.baserow.exception.BaserowUnavailableException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import org.jboss.logging.Logger;

@Provider
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof ResourceNotFoundException e) {
            return buildResponse(
                Response.Status.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                e.getMessage(),
                e.details()
            );
        }

        if (exception instanceof BusinessConflictException e) {
            return buildResponse(
                Response.Status.CONFLICT,
                ErrorCode.BUSINESS_CONFLICT,
                e.getMessage(),
                e.details()
            );
        }

        if (exception instanceof BaserowDataIntegrityException e) {
            LOG.error("Baserow returned incomplete data", exception);

            return buildResponse(
                Response.Status.BAD_GATEWAY,
                ErrorCode.BASEROW_INCOMPLETE_ROW,
                "Coworking active tariff data is incomplete.",
                e.details()
            );
        }

        if (exception instanceof BaserowUnavailableException e) {
            LOG.error("Baserow is unavailable", exception);

            return buildResponse(
                Response.Status.SERVICE_UNAVAILABLE,
                ErrorCode.BASEROW_UNAVAILABLE,
                "External data source is temporarily unavailable.",
                e.details()
            );
        }

        if (exception instanceof AppException e) {
            LOG.error("Unhandled application exception", exception);

            return buildResponse(
                Response.Status.BAD_REQUEST,
                ErrorCode.INTERNAL_SERVER_ERROR,
                e.getMessage(),
                e.details()
            );
        }

        // JAX-RS HTTP exceptions (405, 406, 415, etc.) must pass through with their
        // own status code — they are framework-level responses, not application bugs.
        if (exception instanceof WebApplicationException e) {
            int status = e.getResponse().getStatus();
            LOG.warnf("JAX-RS HTTP %d: %s", status, e.getMessage());
            return buildResponse(
                Response.Status.fromStatusCode(status),
                ErrorCode.INTERNAL_SERVER_ERROR,
                e.getMessage() != null ? e.getMessage() : "HTTP " + status,
                Map.of()
            );
        }

        LOG.error("Unhandled internal error", exception);

        return buildResponse(
            Response.Status.INTERNAL_SERVER_ERROR,
            ErrorCode.INTERNAL_SERVER_ERROR,
            "Internal server error.",
            Map.of()
        );
    }

    private Response buildResponse(
        Response.Status status,
        ErrorCode code,
        String message,
        Map<String, Object> details
    ) {
        var body = new ErrorResponse(
            code.name(),
            message,
            details == null ? Map.of() : details
        );

        return Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .entity(body)
            .build();
    }
}
