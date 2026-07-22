package com.lan.app.infrastructure.baserow.repository;

import com.baserow.repository.AbstractBaserowRepository;
import com.lan.app.domain.model.SupportComplaint;
import com.lan.app.infrastructure.baserow.client.BaserowSupportComplaintClient;
import com.lan.app.infrastructure.baserow.mapper.BaserowSupportComplaintMapper;
import com.lan.app.repository.SupportComplaintRepository;
import com.lan.app.service.command.CreateSupportComplaintCommand;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class BaserowSupportComplaintRepository extends AbstractBaserowRepository implements SupportComplaintRepository {

    private final int complaintsTableId;
    private final BaserowSupportComplaintClient client;
    private final BaserowSupportComplaintMapper mapper;

    BaserowSupportComplaintRepository(
        @ConfigProperty(name = "baserow.support.complaints-table-id") int complaintsTableId,
        @RestClient BaserowSupportComplaintClient client,
        BaserowSupportComplaintMapper mapper
    ) {
        this.complaintsTableId = complaintsTableId;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public SupportComplaint create(CreateSupportComplaintCommand cmd) {
        return execute(() -> {
            var req = mapper.toBaserowRequest(cmd);
            var row = client.create(complaintsTableId, req);
            return mapper.toDomain(row, cmd.topic());
        });
    }
}
