package com.lan.app.api.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/secured")
@Produces(MediaType.TEXT_PLAIN)
public class SecuredResource {

    JsonWebToken jwt;

    @GET
    @Path("/public")
    @PermitAll
    public String publicEndpoint() {
        return "public ok";
    }

    @GET
    @Path("/user")
    @RolesAllowed({"admin", "web-users"})
    public String userEndpoint() {
        return "hello " + jwt.getName() + ", groups=" + jwt.getGroups();
    }
}
