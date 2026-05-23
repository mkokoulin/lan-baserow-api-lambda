package com.lan.app.api.resource;

import com.lan.app.service.PaymentService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

@Path("/events/v1/payments")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentResource {

    private static final Logger log = Logger.getLogger(PaymentResource.class);

    private final PaymentService service;
    private final RegistrationPaidStore paidStore;

    public PaymentResource(PaymentService service, RegistrationPaidStore paidStore) {
        this.service = service;
        this.paidStore = paidStore;
    }

    @POST
    @RolesAllowed({"admin", "web-users"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(
        @RestForm("regId") String regId,
        @RestForm("guestName") String guestName,
        @RestForm("phone") String phone,
        @RestForm("eventName") String eventName,
        @RestForm("amount") String amount,
        @RestForm("file") FileUpload file
    ) {
        byte[] fileBytes = null;
        String filename = "payment.jpg";
        if (file != null) {
            try {
                fileBytes = Files.readAllBytes(file.filePath());
                filename = file.fileName() != null ? file.fileName() : filename;
            } catch (IOException e) {
                log.warnf("Could not read uploaded file: %s", e.getMessage());
            }
        }

        BigDecimal amountDecimal = null;
        if (amount != null && !amount.isBlank()) {
            try { amountDecimal = new BigDecimal(amount); } catch (NumberFormatException ignored) {}
        }

        UUID paymentId = service.createPayment(
            regId, eventName, guestName, phone, amountDecimal, fileBytes, filename
        );

        return Response.ok(Map.of("paymentId", paymentId.toString())).build();
    }

    @POST
    @Path("/{paymentId}/approve")
    @PermitAll
    public Response approve(@PathParam("paymentId") String paymentId) {
        UUID uuid;
        try { uuid = UUID.fromString(paymentId); }
        catch (IllegalArgumentException e) { return Response.status(400).build(); }

        var result = service.approve(uuid);

        if (result.registrationId() != null && !result.registrationId().isBlank()) {
            paidStore.markPaid(result.registrationId());
        }
        result.chatId().ifPresent(id -> log.infof("Payment %s approved, chatId=%d", paymentId, id));

        return Response.ok(Map.of("chatId", result.chatId().orElse(null))).build();
    }

    @POST
    @Path("/{paymentId}/reject")
    @PermitAll
    public Response reject(@PathParam("paymentId") String paymentId) {
        UUID uuid;
        try { uuid = UUID.fromString(paymentId); }
        catch (IllegalArgumentException e) { return Response.status(400).build(); }

        var chatId = service.reject(uuid);
        return Response.ok(Map.of("chatId", chatId.orElse(null))).build();
    }
}
