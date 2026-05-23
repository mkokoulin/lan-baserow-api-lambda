package com.lan.app.infrastructure.baserow.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lan.app.infrastructure.baserow.client.BaserowEventGuestClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.client.BaserowPaymentClient;
import com.lan.app.infrastructure.baserow.dto.BaserowFile;
import com.lan.app.infrastructure.baserow.dto.BaserowPaymentRow;
import com.lan.app.infrastructure.baserow.dto.CreatePaymentRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdatePaymentRow;
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
    private final BaserowEventGuestClient guestClient;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public BaserowPaymentRepository(
        @ConfigProperty(name = "baserow.coworking.payments-table-id") int paymentsTableId,
        @ConfigProperty(name = "baserow.events.registrations-table-id") int registrationsTableId,
        @ConfigProperty(name = "baserow.events.guests-table-id") int guestsTableId,
        @ConfigProperty(name = "baserow.token") String baserowToken,
        @RestClient BaserowPaymentClient client,
        @RestClient BaserowEventRegistrationClient registrationClient,
        @RestClient BaserowEventGuestClient guestClient
    ) {
        this.paymentsTableId = paymentsTableId;
        this.registrationsTableId = registrationsTableId;
        this.guestsTableId = guestsTableId;
        this.baserowToken = baserowToken;
        this.client = client;
        this.registrationClient = registrationClient;
        this.guestClient = guestClient;
    }

    @Override
    public PaymentRepository.CreateResult create(String registrationId, String eventName, String guestName,
                                                  String phone, BigDecimal amount, byte[] fileBytes, String filename) {
        UUID externalId = UUID.randomUUID();
        String proofUrl = null;

        List<Map<String, String>> proofRef = List.of();
        if (fileBytes != null && fileBytes.length > 0) {
            try {
                BaserowFile uploaded = uploadFileToBaserow(fileBytes, filename);
                proofRef = List.of(Map.of("name", uploaded.name()));
                proofUrl = uploaded.url();
            } catch (Exception e) {
                log.warnf("Failed to upload payment proof to Baserow: %s", e.getMessage());
            }
        }

        var req = new CreatePaymentRowRequest(
            externalId,
            registrationId,
            eventName,
            guestName,
            phone,
            amount,
            proofRef,
            "pending"
        );

        execute(() -> client.create(paymentsTableId, req));
        return new PaymentRepository.CreateResult(externalId, proofUrl);
    }

    @Override
    public PaymentRepository.ApproveResult approve(UUID paymentExternalId) {
        var results = execute(() -> client.findByExternalId(paymentsTableId, paymentExternalId)).results();
        if (results.isEmpty()) return new PaymentRepository.ApproveResult(Optional.empty(), null);

        var payment = results.getFirst();
        execute(() -> client.updateStatus(paymentsTableId, payment.id(), new UpdatePaymentRow("approved")));

        String registrationId = payment.registrationId();
        Optional<Long> chatId = markRegistrationPaidAndGetChatId(registrationId, payment);
        return new PaymentRepository.ApproveResult(chatId, registrationId);
    }

    @Override
    public Optional<Long> reject(UUID paymentExternalId) {
        var results = execute(() -> client.findByExternalId(paymentsTableId, paymentExternalId)).results();
        if (results.isEmpty()) return Optional.empty();

        var payment = results.getFirst();
        execute(() -> client.updateStatus(paymentsTableId, payment.id(), new UpdatePaymentRow("rejected")));

        return resolveGuestChatId(payment);
    }

    private Optional<Long> markRegistrationPaidAndGetChatId(String registrationId, BaserowPaymentRow payment) {
        if (registrationId == null || registrationId.isBlank()) return Optional.empty();
        try {
            UUID regUuid = UUID.fromString(registrationId);
            var regResults = execute(() -> registrationClient.findByExternalIdRaw(registrationsTableId, regUuid)).results();
            if (regResults.isEmpty()) return Optional.empty();
            var reg = regResults.getFirst();

            execute(() -> registrationClient.updateIsPaid(
                registrationsTableId, reg.id(),
                new com.lan.app.infrastructure.baserow.dto.UpdateRegistrationIsPaidRequest(true)
            ));

            if (reg.guestId() == null || reg.guestId().isEmpty()) return Optional.empty();
            var guest = execute(() -> guestClient.getByRowId(guestsTableId, reg.guestId().getFirst().id()));
            return Optional.ofNullable(guest.chatId());
        } catch (Exception e) {
            log.warnf("Could not mark registration paid for payment=%s: %s", payment.externalId(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Long> resolveGuestChatId(BaserowPaymentRow payment) {
        if (payment.registrationId() == null || payment.registrationId().isBlank()) return Optional.empty();
        try {
            UUID regUuid = UUID.fromString(payment.registrationId());
            var regResults = execute(() -> registrationClient.findByExternalIdRaw(registrationsTableId, regUuid)).results();
            if (regResults.isEmpty()) return Optional.empty();
            var reg = regResults.getFirst();
            if (reg.guestId() == null || reg.guestId().isEmpty()) return Optional.empty();
            var guest = execute(() -> guestClient.getByRowId(guestsTableId, reg.guestId().getFirst().id()));
            return Optional.ofNullable(guest.chatId());
        } catch (Exception e) {
            log.warnf("Could not resolve chatId for payment=%s: %s", payment.externalId(), e.getMessage());
            return Optional.empty();
        }
    }

    private BaserowFile uploadFileToBaserow(byte[] fileBytes, String filename) throws Exception {
        String boundary = "Boundary" + System.currentTimeMillis();
        byte[] header =("--" + boundary + "\r\n"
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
            throw new RuntimeException("Baserow file upload failed: " + response.statusCode() + " " + response.body());
        }
        return mapper.readValue(response.body(), BaserowFile.class);
    }
}
