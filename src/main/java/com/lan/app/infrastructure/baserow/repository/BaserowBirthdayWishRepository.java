package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.BirthdayWish;
import com.lan.app.infrastructure.baserow.client.BaserowBirthdayWishClient;
import com.lan.app.infrastructure.baserow.dto.CreateBirthdayWishRowRequest;
import com.lan.app.infrastructure.baserow.mapper.BaserowBirthdayWishMapper;
import com.lan.app.repository.BirthdayWishRepository;
import com.lan.app.service.command.CreateBirthdayWishCommand;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class BaserowBirthdayWishRepository implements BirthdayWishRepository {

    private final int tableId;
    private final BaserowBirthdayWishClient client;
    private final BaserowBirthdayWishMapper mapper;

    BaserowBirthdayWishRepository(
        @ConfigProperty(name = "baserow.coworking.birthday-wishes-table-id") int tableId,
        @RestClient BaserowBirthdayWishClient client,
        BaserowBirthdayWishMapper mapper
    ) {
        this.tableId = tableId;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public List<BirthdayWish> list() {
        return client.list(tableId).results().stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public BirthdayWish create(CreateBirthdayWishCommand cmd) {
        var row = client.create(tableId, new CreateBirthdayWishRowRequest(
            cmd.authorName(),
            cmd.telegramUserId(),
            cmd.text()
        ));
        return mapper.toDomain(row);
    }
}
