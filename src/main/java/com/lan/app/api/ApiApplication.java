package com.lan.app.api;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@SuppressWarnings("unused")
@OpenAPIDefinition(
    info = @Info(
        title = "LAN Baserow API",
        version = "1.0.0",
        description = "REST API for working with coworking and events through Baserow",
        contact = @Contact(name = "LAN Team", email = "support@example.com")
    )
)
@SecurityScheme(
    securitySchemeName = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class ApiApplication extends Application {
}