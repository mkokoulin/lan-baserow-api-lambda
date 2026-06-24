package com.lan.app.domain.model;

import java.util.UUID;

public record Id(Integer internalId, UUID externalId) {}
