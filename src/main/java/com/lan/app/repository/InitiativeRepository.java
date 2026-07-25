package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Initiative;

public interface InitiativeRepository {
    List<Initiative> list();
    Initiative get(UUID externalId);
}
