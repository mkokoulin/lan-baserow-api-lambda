package com.lan.app.infrastructure.security.jwt;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class GuestTokenService {

    private static final String ISSUER = "lan-auth";

    @ConfigProperty(name = "app.guest-token.ttl-days", defaultValue = "7")
    int ttlDays;

    public String generateToken(UUID guestExternalId) {
        return Jwt.issuer(ISSUER)
            .upn(guestExternalId.toString())
            .groups(Set.of("guest"))
            .expiresAt(Instant.now().plus(ttlDays, ChronoUnit.DAYS))
            .sign();
    }
}
