package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.CoworkingNew;

public interface CoworkingNewRepository {
    List<CoworkingNew> list();
    CoworkingNew get(UUID externalId);
}