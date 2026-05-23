package com.lan.app.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    record CreateResult(UUID id, String proofUrl) {}

    record ApproveResult(Optional<Long> chatId, String registrationId) {}

    CreateResult create(String registrationId, String eventName, String guestName, String phone,
                        BigDecimal amount, byte[] fileBytes, String filename);

    ApproveResult approve(UUID paymentExternalId);

    Optional<Long> reject(UUID paymentExternalId);
}
