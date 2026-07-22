package com.lan.app.api.mapper;

import com.lan.app.api.dto.request.CreateSupportComplaintRequest;
import com.lan.app.api.dto.response.SupportComplaintResponse;
import com.lan.app.domain.model.SupportComplaint;
import com.lan.app.service.command.CreateSupportComplaintCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiSupportComplaintMapper {

    public CreateSupportComplaintCommand toCommand(CreateSupportComplaintRequest req) {
        return new CreateSupportComplaintCommand(
            req.name(),
            req.phone(),
            req.telegram(),
            req.topic(),
            req.topicCustom(),
            req.comment()
        );
    }

    public SupportComplaintResponse toResponse(SupportComplaint complaint) {
        return new SupportComplaintResponse("Новая");
    }
}
