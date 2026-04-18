package com.lan.app.api.resource;

import com.lan.app.api.dto.response.CoworkingTariffResponse;
import com.lan.app.api.mapper.ApiCoworkingTariffMapper;
import com.lan.app.service.CoworkingTariffService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/coworking/tariffs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Coworking Tariffs",
    description = "Manage coworking tariffs: retrieve the full list or fetch a single tariff by its identifier"
)
@SecurityRequirement(name = "bearerAuth")
public class CoworkingTariffResource {

    private final CoworkingTariffService service;
    private final ApiCoworkingTariffMapper mapper;

    public CoworkingTariffResource(
        CoworkingTariffService service,
        ApiCoworkingTariffMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @Operation(
        operationId = "listCoworkingTariffs",
        summary = "List all coworking tariffs",
        description = "Returns the full list of available coworking tariffs. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Tariffs retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    implementation = CoworkingTariffResponse.class,
                    type = SchemaType.ARRAY
                )
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "User is not authenticated"
        ),
        @APIResponse(
            responseCode = "403",
            description = "User does not have permission to access this resource"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public List<CoworkingTariffResponse> list() {
        var tariffs = service.list();
        return tariffs.stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    @Operation(
        operationId = "getCoworkingTariffById",
        summary = "Get a coworking tariff by its identifier",
        description = "Returns detailed information about a coworking tariff identified by its external UUID. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Tariff found and returned successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingTariffResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "User is not authenticated"
        ),
        @APIResponse(
            responseCode = "403",
            description = "User does not have permission to access this resource"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Tariff with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public CoworkingTariffResponse get(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the tariff (UUID)",
            required = true,
            in = ParameterIn.PATH,
            example = "550e8400-e29b-41d4-a716-446655440000",
            schema = @Schema(type = SchemaType.STRING, format = "uuid")
        )
        @PathParam("externalId") UUID externalId
    ) {
        var tariff = service.get(externalId);
        return mapper.toResponse(tariff);
    }
}