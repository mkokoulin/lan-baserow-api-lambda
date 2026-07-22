package com.lan.app.repository;

import com.lan.app.domain.model.SupportComplaint;
import com.lan.app.service.command.CreateSupportComplaintCommand;

public interface SupportComplaintRepository {
    SupportComplaint create(CreateSupportComplaintCommand cmd);
}
