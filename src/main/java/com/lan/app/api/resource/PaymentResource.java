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
                log.errorf(e, "Could not read uploaded payment file filename=%s: %s", filename, e.getMessage());
            }
        }

        BigDecimal amountDecimal = null;
        if (amount != null && !amount.isBlank()) {
            try {
                amountDecimal = new BigDecimal(amount);
            } catch (NumberFormatException e) {
                log.warnf("Invalid amount value '%s', ignoring", amount);
            }
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
        try {
            uuid = UUID.fromString(paymentId);
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(Map.of("error", "invalid paymentId")).build();
        }

        var result = service.approve(uuid);

        if (result.registrationId() == null) {
            log.warnf("Payment not found for approve: paymentId=%s", paymentId);
            return Response.status(404).entity(Map.of("error", "payment not found")).build();
        }

        if (!result.registrationId().isBlank()) {
            paidStore.markPaid(result.registrationId());
        }
        result.chatId().ifPresent(id -> log.infof("Payment %s approved, notifying chatId=%d", paymentId, id));

        return Response.ok(Map.of("chatId", result.chatId().orElse(null))).build();
    }

    @POST
    @Path("/{paymentId}/reject")
    @PermitAll
    public Response reject(@PathParam("paymentId") String paymentId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(paymentId);
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(Map.of("error", "invalid paymentId")).build();
        }

        var result = service.reject(uuid);

        if (!result.found()) {
            log.warnf("Payment not found for reject: paymentId=%s", paymentId);
            return Response.status(404).entity(Map.of("error", "payment not found")).build();
        }

        result.chatId().ifPresent(id -> log.infof("Payment %s rejected, notifying chatId=%d", paymentId, id));
        return Response.ok(Map.of("chatId", result.chatId().orElse(null))).build();
    }
}
