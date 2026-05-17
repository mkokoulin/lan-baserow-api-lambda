package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Festivale;

public interface FestivaleRepository {
    List<Festivale> list();
    Festivale get(UUID externalId);
}
