package com.lan.app.api.resource;

import java.util.List;
import java.util.UUID;

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

import com.lan.app.api.dto.response.VacancyResponse;
import com.lan.app.api.mapper.ApiVacancyMapper;
import com.lan.app.service.VacancyService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;


@Path("/careers/v1/vacancies")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Vacancies",
    description = "Manage job vacancies: retrieve the full list or fetch a single vacancy by its identifier"
)
@SecurityRequirement(name = "bearerAuth")
public class VacancyResource {

    private final VacancyService service;
    private final ApiVacancyMapper mapper;

    public VacancyResource(
        VacancyService service,
        ApiVacancyMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @Operation(
        operationId = "listVacancies",
        summary = "List all vacancies",
        description = "Returns the full list of published job vacancies. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Vacancies retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    implementation = VacancyResponse.class,
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
    public List<VacancyResponse> list() {
        var vacancies = service.list();
        return vacancies.stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    @Operation(
        operationId = "getVacancyById",
        summary = "Get a vacancy by its identifier",
        description = "Returns detailed information about a vacancy identified by its external UUID. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Vacancy found and returned successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = VacancyResponse.class)
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
            description = "Vacancy with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public VacancyResponse get(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the vacancy (UUID)",
            required = true,
            in = ParameterIn.PATH,
            example = "550e8400-e29b-41d4-a716-446655440000",
            schema = @Schema(type = SchemaType.STRING, format = "uuid")
        )
        @PathParam("externalId") UUID externalId
    ) {
        var vacancy = service.get(externalId);
        return mapper.toResponse(vacancy);
    }
}
