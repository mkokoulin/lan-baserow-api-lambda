package com.lan.app.infrastructure.security.jwt;
import io.smallrye.jwt.build.Jwt;

import java.util.Set;

public class GenerateToken {

    private static final String ISSUER = "lan-auth";

    public static void main(String[] args) {

        if (args.length == 0) {
            throw new IllegalArgumentException(
                    "Usage: GenerateToken <lan-site | lan-bot | admin>"
            );
        }

        String type = args[0];

        String token = switch (type) {

            case "lan-site" -> Jwt.issuer(ISSUER)
                    .upn("lan-site")
                    .groups(Set.of("web-users"))
                    .claim("client_id", "lan-site")
                    .sign();

            case "lan-bot" -> Jwt.issuer(ISSUER)
                    .upn("lan-bot")
                    .groups(Set.of("web-users"))
                    .claim("client_id", "lan-bot")
                    .sign();

            case "admin" -> Jwt.issuer(ISSUER)
                    .upn("admin")
                    .groups(Set.of("admin"))
                    .claim("client_id", "admin")
                    .sign();

            default -> throw new IllegalArgumentException(
                    "Unknown token type: " + type
            );
        };

        System.out.println(token);
    }
}