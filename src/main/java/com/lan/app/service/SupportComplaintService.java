package com.lan.app.service;

import com.lan.app.domain.model.SupportComplaint;
import com.lan.app.repository.SupportComplaintRepository;
import com.lan.app.service.command.CreateSupportComplaintCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SupportComplaintService {

    private final SupportComplaintRepository repo;

    public SupportComplaintService(SupportComplaintRepository repo) {
        this.repo = repo;
    }

    public SupportComplaint create(CreateSupportComplaintCommand cmd) {
        return repo.create(cmd);
    }
}
