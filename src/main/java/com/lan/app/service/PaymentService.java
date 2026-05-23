package com.lan.app.service;

import com.lan.app.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.UUID;

@ApplicationScoped
public class PaymentService {

    private final PaymentRepository paymentRepo;

    public PaymentService(PaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    public UUID createPayment(String registrationId, String eventName, String guestName, String phone,
                              BigDecimal amount, byte[] fileBytes, String filename) {
        var result = paymentRepo.create(registrationId, eventName, guestName, phone,
                amount, fileBytes, filename);
        return result.id();
    }

    public PaymentRepository.ApproveResult approve(UUID paymentId) {
        return paymentRepo.approve(paymentId);
    }

    public PaymentRepository.RejectResult reject(UUID paymentId) {
        return paymentRepo.reject(paymentId);
    }
}
