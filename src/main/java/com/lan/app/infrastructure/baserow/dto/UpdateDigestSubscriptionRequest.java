package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateDigestSubscriptionRequest(
    @JsonProperty("digest_subscribed") Boolean digestSubscribed
) {}
