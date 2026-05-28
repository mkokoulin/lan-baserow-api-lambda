package com.lan.app.api.exception;

import com.lan.app.api.dto.response.ErrorResponse;
import com.lan.app.domain.exception.AppException;
import com.lan.app.domain.exception.BusinessConflictException;
import com.lan.app.domain.exception.CorruptedDataException;
import com.lan.app.domain.exception.ExternalServiceException;
import com.lan.app.domain.exception.ExternalServiceRateLimitedException;
import com.lan.app.domain.exception.ExternalServiceUnauthorizedException;
import com.lan.app.domain.exception.ExternalServiceUnavailableException;
import com.lan.app.domain.exception.GuestNotFoundException;
import com.lan.app.domain.exception.NotificationNotFoundException;
import com.lan.app.domain.exception.RegistrationConflictException;
import com.lan.app.domain.exception.RegistrationNotFoundException;
import com.lan.app.domain.exception.ResourceNotFoundException;
import com.lan.app.domain.exception.ValidationException;
import com.lan.app.infrastructure.baserow.exception.BaserowDataIntegrityException;
import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;
import com.lan.app.infrastructure.baserow.exception.BaserowRateLimitedException;
import com.lan.app.infrastructure.baserow.exception.BaserowUnauthorizedException;
import com.lan.app.infrastructure.baserow.exception.BaserowUnavailableException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@Provider
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {

        // 400 — validation errors
        if (exception instanceof ConstraintViolationException e) {
            var fields = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    v -> (Object) v.getMessage(),
                    (a, b) -> a
                ));
            return buildResponse(Response.Status.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "Request validation failed.", fields);
        }

        if (exception instanceof ValidationException e) {
            return buildResponse(Response.Status.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                e.getMessage(), Map.of());
        }

        // 404 — not found
        if (exception instanceof BaserowNotFoundException e) {
            return buildResponse(Response.Status.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                e.getMessage(), Map.of("externalId", e.externalId().toString()));
        }

        if (exception instanceof ResourceNotFoundException e) {
            return buildResponse(Response.Status.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                e.getMessage(), e.details());
        }

        if (exception instanceof GuestNotFoundException e) {
            return buildResponse(Response.Status.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                e.getMessage(), Map.of());
        }

        if (exception instanceof NotificationNotFoundException e) {
            return buildResponse(Response.Status.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                e.getMessage(), Map.of());
        }

        if (exception instanceof RegistrationNotFoundException e) {
            return buildResponse(Response.Status.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                e.getMessage(), Map.of());
        }

        // 409 — conflict
        if (exception instanceof BusinessConflictException e) {
            return buildResponse(Response.Status.CONFLICT, ErrorCode.BUSINESS_CONFLICT,
                e.getMessage(), e.details());
        }

        if (exception instanceof RegistrationConflictException e) {
            return buildResponse(Response.Status.CONFLICT, ErrorCode.BUSINESS_CONFLICT,
                e.getMessage(), Map.of());
        }

        // 429 — rate limit
        if (exception instanceof BaserowRateLimitedException e) {
            LOG.warn("Baserow rate limit exceeded");
            return buildResponse(Response.Status.fromStatusCode(429), ErrorCode.RATE_LIMITED,
                "Too many requests to external service. Please retry later.", Map.of());
        }

        if (exception instanceof ExternalServiceRateLimitedException e) {
            LOG.warn("External service rate limit exceeded");
            return buildResponse(Response.Status.fromStatusCode(429), ErrorCode.RATE_LIMITED,
                "Too many requests to external service. Please retry later.", Map.of());
        }

        // 502 — upstream data / auth problems
        if (exception instanceof BaserowDataIntegrityException e) {
            LOG.error("Baserow returned incomplete data", exception);
            return buildResponse(Response.Status.BAD_GATEWAY, ErrorCode.BASEROW_INCOMPLETE_ROW,
                e.getMessage(), Map.of());
        }

        if (exception instanceof CorruptedDataException e) {
            LOG.error("Corrupted data received from upstream", exception);
            return buildResponse(Response.Status.BAD_GATEWAY, ErrorCode.BASEROW_INCOMPLETE_ROW,
                "External data source returned corrupted data.", Map.of());
        }

        if (exception instanceof BaserowUnauthorizedException e) {
            LOG.error("Baserow token is invalid or missing", exception);
            return buildResponse(Response.Status.BAD_GATEWAY, ErrorCode.EXTERNAL_AUTH_ERROR,
                "External service rejected our credentials.", Map.of());
        }

        if (exception instanceof ExternalServiceUnauthorizedException e) {
            LOG.error("External service rejected our credentials", exception);
            return buildResponse(Response.Status.BAD_GATEWAY, ErrorCode.EXTERNAL_AUTH_ERROR,
                "External service rejected our credentials.", Map.of());
        }

        if (exception instanceof ExternalServiceException e) {
            LOG.error("External service error", exception);
            return buildResponse(Response.Status.BAD_GATEWAY, ErrorCode.UPSTREAM_ERROR,
                "External service returned an unexpected error.", Map.of());
        }

        // 503 — upstream unavailable
        if (exception instanceof BaserowUnavailableException e) {
            LOG.error("Baserow is unavailable", exception);
            return buildResponse(Response.Status.SERVICE_UNAVAILABLE, ErrorCode.BASEROW_UNAVAILABLE,
                "External data source is temporarily unavailable.", Map.of());
        }

        if (exception instanceof ExternalServiceUnavailableException e) {
            LOG.error("External service is unavailable", exception);
            return buildResponse(Response.Status.SERVICE_UNAVAILABLE, ErrorCode.BASEROW_UNAVAILABLE,
                "External data source is temporarily unavailable.", Map.of());
        }

        // JAX-RS HTTP exceptions (405, 406, 415, etc.) pass through with their own status code
        if (exception instanceof WebApplicationException e) {
            int status = e.getResponse().getStatus();
            LOG.warnf("JAX-RS HTTP %d: %s", status, e.getMessage());
            return buildResponse(Response.Status.fromStatusCode(status), ErrorCode.INTERNAL_SERVER_ERROR,
                e.getMessage() != null ? e.getMessage() : "HTTP " + status, Map.of());
        }

        // Generic AppException — unhandled application-level error
        if (exception instanceof AppException e) {
            LOG.error("Unhandled application exception", exception);
            return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR,
                "Internal server error.", e.details());
        }

        // Catch-all
        LOG.error("Unhandled internal error", exception);
        return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR,
            "Internal server error.", Map.of());
    }

    private Response buildResponse(Response.Status status, ErrorCode code, String message, Map<String, Object> details) {
        return Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .entity(new ErrorResponse(code.name(), message, details == null ? Map.of() : details))
            .build();
    }
}
