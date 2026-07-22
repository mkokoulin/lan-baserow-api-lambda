package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Festival;

public interface EventsFestivalRepository {
    List<Festival> list();
    Festival get(UUID externalId);
}
