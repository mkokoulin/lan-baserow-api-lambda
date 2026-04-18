package com.lan.app.api.resource;

import com.lan.app.api.dto.response.CoworkingActiveTariffListItemResponse;
import com.lan.app.api.dto.response.CoworkingActiveTariffResponse;
import com.lan.app.api.mapper.ApiCoworkingActiveTariffMapper;
import com.lan.app.service.CoworkingActiveTariffService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
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

@Path("/coworking/active-tariffs")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Coworking Active Tariffs",
    description = "Manage active coworking tariffs: retrieve the list of currently active tariffs or fetch detailed information about a specific one"
)
@SecurityRequirement(name = "bearerAuth")
public class CoworkingActiveTariffResource {

    private final CoworkingActiveTariffService service;
    private final ApiCoworkingActiveTariffMapper mapper;

    public CoworkingActiveTariffResource(
        CoworkingActiveTariffService service,
        ApiCoworkingActiveTariffMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @Operation(
        operationId = "listCoworkingActiveTariffs",
        summary = "List all active coworking tariffs",
        description = "Returns a lightweight list of all currently active coworking tariffs. " +
            "Each item contains only the fields required for list views. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Active tariffs retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    implementation = CoworkingActiveTariffListItemResponse.class,
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
    public List<CoworkingActiveTariffListItemResponse> list() {
        var tariffs = service.list();
        return tariffs.stream()
            .map(mapper::toListItem)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    @Operation(
        operationId = "getCoworkingActiveTariffById",
        summary = "Get an active coworking tariff by its identifier",
        description = "Returns the full detailed representation of an active coworking tariff identified by its external UUID. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Active tariff found and returned successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingActiveTariffResponse.class)
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
            description = "Active tariff with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public CoworkingActiveTariffResponse get(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the active tariff (UUID)",
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