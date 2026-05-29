package com.lan.app.infrastructure.baserow.repository;
import com.baserow.repository.AbstractBaserowRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.client.BaserowPaymentClient;
import com.baserow.dto.BaserowFile;
import com.lan.app.infrastructure.baserow.dto.BaserowPaymentRow;
import com.lan.app.infrastructure.baserow.dto.CreatePaymentRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdatePaymentRow;
import com.lan.app.infrastructure.baserow.dto.UpdateRegistrationIsPaidRequest;
import com.lan.app.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BaserowPaymentRepository extends AbstractBaserowRepository implements PaymentRepository {

    private static final Logger log = Logger.getLogger(BaserowPaymentRepository.class);
    private static final String BASEROW_API = "https://api.baserow.io";

    private final int paymentsTableId;
    private final int registrationsTableId;
    private final int guestsTableId;
    private final String baserowToken;

    private final BaserowPaymentClient client;
    private final BaserowEventRegistrationClient registrationClient;
    private final BaserowGuestClient guestClient;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper;

    @Inject
    public BaserowPaymentRepository(
        @ConfigProperty(name = "baserow.events.payments-table-id") int paymentsTableId,
        @ConfigProperty(name = "baserow.events.registrations-table-id") int registrationsTableId,
        @ConfigProperty(name = "baserow.guests.guests-table-id") int guestsTableId,
        @ConfigProperty(name = "baserow.token") String baserowToken,
        @RestClient BaserowPaymentClient client,
        @RestClient BaserowEventRegistrationClient registrationClient,
        @RestClient BaserowGuestClient guestClient,
        ObjectMapper mapper
    ) {
        this.paymentsTableId = paymentsTableId;
        this.registrationsTableId = registrationsTableId;
        this.guestsTableId = guestsTableId;
        this.baserowToken = baserowToken;
        this.client = client;
        this.registrationClient = registrationClient;
        this.guestClient = guestClient;
        this.mapper = mapper;
    }

    @Override
    public PaymentRepository.CreateResult create(String registrationId, String eventName, String guestName,
                                                  String phone, BigDecimal amount, byte[] fileBytes, String filename) {
        String proofUrl = null;

        List<Map<String, String>> proofRef = List.of();
        if (fileBytes != null && fileBytes.length > 0) {
            try {
                BaserowFile uploaded = uploadFileToBaserow(fileBytes, filename);
                proofRef = List.of(Map.of("name", uploaded.name()));
                proofUrl = uploaded.url();
                log.infof("Uploaded payment proof filename=%s: url=%s", filename, proofUrl);
            } catch (Exception e) {
                log.errorf(e, "Failed to upload payment proof filename=%s: %s", filename, e.getMessage());
            }
        }

        List<Integer> registrationIdRef = List.of();
        try {
            UUID regUuid = UUID.fromString(registrationId);
            var regResults = execute(() -> registrationClient.findByExternalIdRaw(registrationsTableId, regUuid)).results();
            if (!regResults.isEmpty()) {
                registrationIdRef = List.of(regResults.getFirst().id());
            } else {
                log.warnf("Registration not found in Baserow for registrationId=%s", registrationId);
            }
        } catch (Exception e) {
            log.errorf(e, "Failed to resolve registration row id for registrationId=%s: %s", registrationId, e.getMessage());
        }

        var req = new CreatePaymentRowRequest(
            registrationIdRef,
            eventName,
            guestName,
            phone,
            amount,
            proofRef,
            "pending"
        );

        var created = execute(() -> client.create(paymentsTableId, req));
        UUID externalId = created.externalId();
        log.infof("Created payment row externalId=%s registrationId=%s proofUploaded=%b",
            externalId, registrationId, proofUrl != null);
        return new PaymentRepository.CreateResult(externalId, proofUrl);
    }

    @Override
    public PaymentRepository.ApproveResult approve(UUID paymentExternalId) {
        var results = execute(() -> client.findByExternalId(paymentsTableId, paymentExternalId)).results();
        if (results.isEmpty()) {
            log.warnf("Payment not found for approve: externalId=%s", paymentExternalId);
            return new PaymentRepository.ApproveResult(Optional.empty(), null);
        }

        var payment = results.getFirst();
        execute(() -> client.updateStatus(paymentsTableId, payment.id(), new UpdatePaymentRow("approved")));
        log.infof("Payment approved: externalId=%s rowId=%d registrationId=%s",
            paymentExternalId, payment.id(), payment.registrationIdValue());

        String registrationId = payment.registrationIdValue();
        Optional<Long> chatId = markRegistrationPaidAndGetChatId(registrationId, payment);
        return new PaymentRepository.ApproveResult(chatId, registrationId);
    }

    @Override
    public PaymentRepository.RejectResult reject(UUID paymentExternalId) {
        var results = execute(() -> client.findByExternalId(paymentsTableId, paymentExternalId)).results();
        if (results.isEmpty()) {
            log.warnf("Payment not found for reject: externalId=%s", paymentExternalId);
            return new PaymentRepository.RejectResult(false, Optional.empty());
        }

        var payment = results.getFirst();
        execute(() -> client.updateStatus(paymentsTableId, payment.id(), new UpdatePaymentRow("rejected")));
        log.infof("Payment rejected: externalId=%s rowId=%d registrationId=%s",
            paymentExternalId, payment.id(), payment.registrationIdValue());

        return new PaymentRepository.RejectResult(true, resolveGuestChatId(payment));
    }

    private Optional<Long> markRegistrationPaidAndGetChatId(String registrationId, BaserowPaymentRow payment) {
        if (registrationId == null || registrationId.isBlank()) {
            log.warnf("Payment=%s has no registrationId, skipping is_paid update", payment.externalId());
            return Optional.empty();
        }
        UUID regUuid;
        try {
            regUuid = UUID.fromString(registrationId);
        } catch (IllegalArgumentException e) {
            log.errorf("Payment=%s has invalid registrationId=%s (not a UUID)", payment.externalId(), registrationId);
            return Optional.empty();
        }
        try {
            var regResults = execute(() -> registrationClient.findByExternalIdRaw(registrationsTableId, regUuid)).results();
            if (regResults.isEmpty()) {
                log.warnf("Registration not found in Baserow for payment=%s registrationId=%s",
                    payment.externalId(), registrationId);
                return Optional.empty();
            }
            var reg = regResults.getFirst();

            execute(() -> registrationClient.updateIsPaid(
                registrationsTableId, reg.id(),
                new UpdateRegistrationIsPaidRequest(true)
            ));
            log.infof("Marked registration=%s as paid (rowId=%d)", registrationId, reg.id());

            if (reg.guestId() == null || reg.guestId().isEmpty()) {
                log.warnf("Registration=%s has no guestId, cannot resolve chatId", registrationId);
                return Optional.empty();
            }
            var guest = execute(() -> guestClient.getByRowId(guestsTableId, reg.guestId().getFirst().id()));
            return Optional.ofNullable(guest.telegramChatId());
        } catch (Exception e) {
            log.errorf(e, "Failed to mark registration paid for payment=%s registrationId=%s: %s",
                payment.externalId(), registrationId, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Long> resolveGuestChatId(BaserowPaymentRow payment) {
        String registrationId = payment.registrationIdValue();
        if (registrationId == null || registrationId.isBlank()) {
            log.warnf("Payment=%s has no registrationId, cannot resolve chatId", payment.externalId());
            return Optional.empty();
        }
        UUID regUuid;
        try {
            regUuid = UUID.fromString(registrationId);
        } catch (IllegalArgumentException e) {
            log.errorf("Payment=%s has invalid registrationId=%s (not a UUID)", payment.externalId(), registrationId);
            return Optional.empty();
        }
        try {
            var regResults = execute(() -> registrationClient.findByExternalIdRaw(registrationsTableId, regUuid)).results();
            if (regResults.isEmpty()) {
                log.warnf("Registration not found for payment=%s registrationId=%s",
                    payment.externalId(), registrationId);
                return Optional.empty();
            }
            var reg = regResults.getFirst();
            if (reg.guestId() == null || reg.guestId().isEmpty()) {
                log.warnf("Registration=%s has no guestId, cannot resolve chatId for reject", registrationId);
                return Optional.empty();
            }
            var guest = execute(() -> guestClient.getByRowId(guestsTableId, reg.guestId().getFirst().id()));
            return Optional.ofNullable(guest.telegramChatId());
        } catch (Exception e) {
            log.errorf(e, "Failed to resolve chatId for rejected payment=%s registrationId=%s: %s",
                payment.externalId(), registrationId, e.getMessage());
            return Optional.empty();
        }
    }

    private BaserowFile uploadFileToBaserow(byte[] fileBytes, String filename) throws Exception {
        String boundary = "Boundary" + System.currentTimeMillis();
        byte[] header = ("--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n"
            + "Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] footer = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[header.length + fileBytes.length + footer.length];
        System.arraycopy(header, 0, body, 0, header.length);
        System.arraycopy(fileBytes, 0, body, header.length, fileBytes.length);
        System.arraycopy(footer, 0, body, header.length + fileBytes.length, footer.length);

        var request = HttpRequest.newBuilder()
            .uri(URI.create(BASEROW_API + "/api/user-files/upload-file/"))
            .header("Authorization", "Token " + baserowToken)
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Baserow file upload failed with HTTP " + response.statusCode()
                + ": " + response.body());
        }
        return mapper.readValue(response.body(), BaserowFile.class);
    }
}
