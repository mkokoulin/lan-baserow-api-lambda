#!/usr/bin/env bash
# Scaffolds a full read vertical slice (domain model -> repository interface ->
# Baserow DTO/client/mapper -> Baserow repository adapter -> service -> API
# response DTO -> API mapper -> JAX-RS resource) following the pattern used by
# VacancyRepository/BaserowVacancyRepository/VacancyResource etc.
#
# Usage:
#   scripts/new-resource.sh <Entity> <table.config.property> </api/path> [field:Type[?] ...]
#
# Example:
#   scripts/new-resource.sh Partner baserow.careers.partners-table-id /careers/v1/partners \
#       name:String logoUrl:String? websiteUrl:String?
#
# Supported field types: String, Integer, Long, Boolean, Double, UUID.
# Append "?" to a type to make the field nullable (BaserowRow gets @Nullable
# instead of @NotBlank/@NotNull, and the API response Schema gets nullable=true).
#
# What this does NOT do (on purpose — needs a human decision):
#   - Does not pick real Baserow column names/types for you beyond the fields you pass.
#   - Does not add write endpoints (create/update/delete) — only list()/get(UUID),
#     which is the dominant read pattern across the existing resources. A TODO block
#     in the generated resource shows where to add them.
#   - Does not wire the new path into any auth/role config beyond @RolesAllowed.

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$ROOT/src/main/java/com/lan/app"
PROPS_FILE="$ROOT/src/main/resources/application.properties"

die() { echo "error: $*" >&2; exit 1; }

[ $# -ge 3 ] || die "usage: $0 <Entity> <table.config.property> </api/path> [field:Type[?] ...]"

ENTITY="$1"; shift
TABLE_CONFIG="$1"; shift
API_PATH="$1"; shift
FIELD_SPECS=("$@")

[[ "$ENTITY" =~ ^[A-Z][A-Za-z0-9]*$ ]] || die "Entity must be PascalCase, e.g. Partner (got: $ENTITY)"

ENTITY_LOWER="$(printf '%s' "${ENTITY:0:1}" | tr '[:upper:]' '[:lower:]')${ENTITY:1}"
TAG="${ENTITY}s"
ROLES='"admin", "web-users"'

camel_to_snake() {
    printf '%s' "$1" | sed -E 's/([a-z0-9])([A-Z])/\1_\2/g; s/([A-Z]+)([A-Z][a-z])/\1_\2/g' | tr '[:upper:]' '[:lower:]'
}

# --- parse field specs into four parallel template fragments ---
DOMAIN_FIELDS=""
ROW_FIELDS=""
RESPONSE_FIELDS=""
ROW_TO_DOMAIN_ARGS=""
DOMAIN_TO_RESPONSE_ARGS=""

for spec in "${FIELD_SPECS[@]+"${FIELD_SPECS[@]}"}"; do
    name="${spec%%:*}"
    typespec="${spec#*:}"
    [ "$name" != "$spec" ] || die "bad field spec '$spec', expected name:Type or name:Type?"

    nullable=false
    type="$typespec"
    if [[ "$typespec" == *\? ]]; then
        nullable=true
        type="${typespec%\?}"
    fi

    case "$type" in
        String|Integer|Long|Boolean|Double|UUID) ;;
        *) die "unsupported field type '$type' for field '$name' (supported: String, Integer, Long, Boolean, Double, UUID)" ;;
    esac

    snake="$(camel_to_snake "$name")"

    if [ "$nullable" = true ]; then
        row_annotation="@Nullable"
    elif [ "$type" = "String" ]; then
        row_annotation="@NotBlank"
    else
        row_annotation="@NotNull"
    fi

    DOMAIN_FIELDS="${DOMAIN_FIELDS}    ${type} ${name},
"
    ROW_FIELDS="${ROW_FIELDS}    ${row_annotation} @JsonProperty(\"${snake}\") ${type} ${name},
"
    if [ "$nullable" = true ]; then
        RESPONSE_FIELDS="${RESPONSE_FIELDS}
    @Schema(
        description = \"TODO: describe ${name}\",
        nullable = true
    )
    ${type} ${name},
"
    else
        RESPONSE_FIELDS="${RESPONSE_FIELDS}
    @Schema(
        description = \"TODO: describe ${name}\",
        required = true
    )
    ${type} ${name},
"
    fi
    ROW_TO_DOMAIN_ARGS="${ROW_TO_DOMAIN_ARGS}            ${ENTITY_LOWER}.${name}(),
"
    DOMAIN_TO_RESPONSE_ARGS="${DOMAIN_TO_RESPONSE_ARGS}            ${ENTITY_LOWER}.${name}(),
"
done

# --- target files ---
F_DOMAIN="$SRC/domain/model/${ENTITY}.java"
F_REPO_IFACE="$SRC/repository/${ENTITY}Repository.java"
F_BASEROW_ROW="$SRC/infrastructure/baserow/dto/Baserow${ENTITY}Row.java"
F_BASEROW_CLIENT="$SRC/infrastructure/baserow/client/Baserow${ENTITY}Client.java"
F_BASEROW_MAPPER="$SRC/infrastructure/baserow/mapper/Baserow${ENTITY}Mapper.java"
F_BASEROW_REPO="$SRC/infrastructure/baserow/repository/Baserow${ENTITY}Repository.java"
F_SERVICE="$SRC/service/${ENTITY}Service.java"
F_RESPONSE="$SRC/api/dto/response/${ENTITY}Response.java"
F_API_MAPPER="$SRC/api/mapper/Api${ENTITY}Mapper.java"
F_RESOURCE="$SRC/api/resource/${ENTITY}Resource.java"

for f in "$F_DOMAIN" "$F_REPO_IFACE" "$F_BASEROW_ROW" "$F_BASEROW_CLIENT" "$F_BASEROW_MAPPER" \
         "$F_BASEROW_REPO" "$F_SERVICE" "$F_RESPONSE" "$F_API_MAPPER" "$F_RESOURCE"; do
    [ -e "$f" ] && die "already exists, aborting so nothing gets overwritten: $f"
done

mkdir -p "$(dirname "$F_DOMAIN")" "$(dirname "$F_REPO_IFACE")" "$(dirname "$F_BASEROW_ROW")" \
         "$(dirname "$F_BASEROW_CLIENT")" "$(dirname "$F_BASEROW_MAPPER")" "$(dirname "$F_BASEROW_REPO")" \
         "$(dirname "$F_SERVICE")" "$(dirname "$F_RESPONSE")" "$(dirname "$F_API_MAPPER")" "$(dirname "$F_RESOURCE")"

# ---------------------------------------------------------------------------
# 1. domain/model/<Entity>.java
# ---------------------------------------------------------------------------
cat > "$F_DOMAIN" <<EOF
package com.lan.app.domain.model;

public record ${ENTITY}(
    Id id,
${DOMAIN_FIELDS}) {}
EOF
# Id id, always has a trailing comma above, same as DOMAIN_FIELDS's last line (if any) — strip
# whichever one ends up immediately before the closing paren.
sed -i ':a;N;$!ba;s/,\n) {}/\n) {}/' "$F_DOMAIN"

# ---------------------------------------------------------------------------
# 2. repository/<Entity>Repository.java (port)
# ---------------------------------------------------------------------------
cat > "$F_REPO_IFACE" <<EOF
package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.${ENTITY};

public interface ${ENTITY}Repository {
    List<${ENTITY}> list();
    ${ENTITY} get(UUID externalId);
}
EOF

# ---------------------------------------------------------------------------
# 3. infrastructure/baserow/dto/Baserow<Entity>Row.java
# ---------------------------------------------------------------------------
cat > "$F_BASEROW_ROW" <<EOF
package com.lan.app.infrastructure.baserow.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Baserow${ENTITY}Row(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
${ROW_FIELDS}) {}
EOF
# ROW_FIELDS (if non-empty) always ends with a trailing ",\n" before the closing paren — strip it
sed -i ':a;N;$!ba;s/,\n) {}/\n) {}/' "$F_BASEROW_ROW"

# ---------------------------------------------------------------------------
# 4. infrastructure/baserow/client/Baserow<Entity>Client.java
# ---------------------------------------------------------------------------
cat > "$F_BASEROW_CLIENT" <<EOF
package com.lan.app.infrastructure.baserow.client;

import com.baserow.client.BaserowAuthHeaders;

import java.util.UUID;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.lan.app.infrastructure.baserow.dto.Baserow${ENTITY}Row;
import com.baserow.dto.BaserowListResponse;
import com.baserow.exception.BaserowDataIntegrityException;
import com.baserow.exception.BaserowNotFoundException;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "baserow")
@RegisterProvider(BaserowAuthHeaders.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface Baserow${ENTITY}Client {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<Baserow${ENTITY}Row> list(
        @PathParam("tableId") int tableId
    );

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    Baserow${ENTITY}Row getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<Baserow${ENTITY}Row> findAllByExternalId(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__external_id__equal") UUID externalId
    );

    default Baserow${ENTITY}Row findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findAllByExternalId(tableId, externalId);

        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("${ENTITY}", externalId);
        }

        if (resp.results().size() > 1) {
            throw new BaserowDataIntegrityException("${ENTITY}", externalId);
        }

        return resp.results().getFirst();
    }
}
EOF

# ---------------------------------------------------------------------------
# 5. infrastructure/baserow/mapper/Baserow<Entity>Mapper.java
# ---------------------------------------------------------------------------
cat > "$F_BASEROW_MAPPER" <<EOF
package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.Id;
import com.lan.app.domain.model.${ENTITY};
import com.lan.app.infrastructure.baserow.dto.Baserow${ENTITY}Row;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Baserow${ENTITY}Mapper {

    public ${ENTITY} toDomain(Baserow${ENTITY}Row ${ENTITY_LOWER}) {
        return new ${ENTITY}(
            new Id(${ENTITY_LOWER}.id(), ${ENTITY_LOWER}.externalId()),
${ROW_TO_DOMAIN_ARGS}        );
    }
}
EOF
# fix trailing comma before the closing paren (last generated arg still ends with ",\n")
sed -i ':a;N;$!ba;s/,\n        );/\n        );/' "$F_BASEROW_MAPPER"

# ---------------------------------------------------------------------------
# 6. infrastructure/baserow/repository/Baserow<Entity>Repository.java (adapter)
# ---------------------------------------------------------------------------
cat > "$F_BASEROW_REPO" <<EOF
package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.${ENTITY};
import com.lan.app.infrastructure.baserow.client.Baserow${ENTITY}Client;
import com.lan.app.infrastructure.baserow.mapper.Baserow${ENTITY}Mapper;
import com.lan.app.repository.${ENTITY}Repository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Baserow${ENTITY}Repository implements ${ENTITY}Repository {

    private final int tableId;

    private final Baserow${ENTITY}Client client;
    private final Baserow${ENTITY}Mapper mapper;

    Baserow${ENTITY}Repository(
        @ConfigProperty(name = "${TABLE_CONFIG}") int tableId,
        @RestClient Baserow${ENTITY}Client client,
        Baserow${ENTITY}Mapper mapper
    ) {
        this.tableId = tableId;
        this.client = client;
        this.mapper = mapper;
    }

    public List<${ENTITY}> list() {
        var row = client.list(tableId);
        return row.results().stream().map(mapper::toDomain).toList();
    }

    public ${ENTITY} get(UUID externalId) {
        var row = client.findUniqueByExternalId(tableId, externalId);
        return mapper.toDomain(row);
    }
}
EOF

# ---------------------------------------------------------------------------
# 7. service/<Entity>Service.java
# ---------------------------------------------------------------------------
cat > "$F_SERVICE" <<EOF
package com.lan.app.service;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.${ENTITY};
import com.lan.app.repository.${ENTITY}Repository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ${ENTITY}Service {

    ${ENTITY}Repository repo;

    public ${ENTITY}Service(${ENTITY}Repository repo) {
        this.repo = repo;
    }

    public List<${ENTITY}> list() {
        return repo.list();
    }

    public ${ENTITY} get(UUID externalId) {
        return repo.get(externalId);
    }
}
EOF

# ---------------------------------------------------------------------------
# 8. api/dto/response/<Entity>Response.java
# ---------------------------------------------------------------------------
cat > "$F_RESPONSE" <<EOF
package com.lan.app.api.dto.response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "${ENTITY}Response",
    description = "TODO: describe ${ENTITY}Response"
)
public record ${ENTITY}Response(

    @Schema(
        description = "External unique identifier",
        examples = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,
${RESPONSE_FIELDS}) {
}
EOF
# drop trailing comma on the last field before the closing paren
sed -i ':a;N;$!ba;s/,\n) {/\n) {/' "$F_RESPONSE"

# ---------------------------------------------------------------------------
# 9. api/mapper/Api<Entity>Mapper.java
# ---------------------------------------------------------------------------
cat > "$F_API_MAPPER" <<EOF
package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.${ENTITY}Response;
import com.lan.app.domain.model.${ENTITY};

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Api${ENTITY}Mapper {

    public ${ENTITY}Response toResponse(${ENTITY} ${ENTITY_LOWER}) {
        return new ${ENTITY}Response(
            ${ENTITY_LOWER}.id().externalId(),
${DOMAIN_TO_RESPONSE_ARGS}        );
    }
}
EOF
sed -i ':a;N;$!ba;s/,\n        );/\n        );/' "$F_API_MAPPER"

# ---------------------------------------------------------------------------
# 10. api/resource/<Entity>Resource.java
# ---------------------------------------------------------------------------
cat > "$F_RESOURCE" <<EOF
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

import com.lan.app.api.dto.response.${ENTITY}Response;
import com.lan.app.api.mapper.Api${ENTITY}Mapper;
import com.lan.app.service.${ENTITY}Service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("${API_PATH}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({${ROLES}})
@Tag(
    name = "${TAG}",
    description = "TODO: describe the ${TAG} resource"
)
@SecurityRequirement(name = "bearerAuth")
public class ${ENTITY}Resource {

    private final ${ENTITY}Service service;
    private final Api${ENTITY}Mapper mapper;

    public ${ENTITY}Resource(
        ${ENTITY}Service service,
        Api${ENTITY}Mapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @Operation(
        operationId = "list${TAG}",
        summary = "List all ${ENTITY_LOWER}s",
        description = "TODO: describe this endpoint."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "${TAG} retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    implementation = ${ENTITY}Response.class,
                    type = SchemaType.ARRAY
                )
            )
        ),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission to access this resource"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public List<${ENTITY}Response> list() {
        var items = service.list();
        return items.stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    @Operation(
        operationId = "get${ENTITY}ById",
        summary = "Get a ${ENTITY_LOWER} by its identifier",
        description = "TODO: describe this endpoint."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "${ENTITY} found and returned successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ${ENTITY}Response.class)
            )
        ),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission to access this resource"),
        @APIResponse(responseCode = "404", description = "${ENTITY} with the given externalId was not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public ${ENTITY}Response get(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the ${ENTITY_LOWER} (UUID)",
            required = true,
            in = ParameterIn.PATH,
            example = "550e8400-e29b-41d4-a716-446655440000",
            schema = @Schema(type = SchemaType.STRING, format = "uuid")
        )
        @PathParam("externalId") UUID externalId
    ) {
        var item = service.get(externalId);
        return mapper.toResponse(item);
    }

    // TODO: write endpoints (POST/PATCH/DELETE) follow the same shape — add a method to
    // ${ENTITY}Repository + Baserow${ENTITY}Repository (and a request DTO under api/dto/request)
    // the same way list()/get() were added here, then expose it below.
}
EOF

# ---------------------------------------------------------------------------
# application.properties: append a placeholder table-id line if not already present
# ---------------------------------------------------------------------------
ADDED_CONFIG=false
if ! grep -q "^${TABLE_CONFIG}=" "$PROPS_FILE"; then
    printf '\n# TODO: set the real Baserow table id for %s\n%s=REPLACE_ME\n' "$ENTITY" "$TABLE_CONFIG" >> "$PROPS_FILE"
    ADDED_CONFIG=true
fi

echo "Generated ${ENTITY} vertical slice:"
for f in "$F_DOMAIN" "$F_REPO_IFACE" "$F_BASEROW_ROW" "$F_BASEROW_CLIENT" "$F_BASEROW_MAPPER" \
         "$F_BASEROW_REPO" "$F_SERVICE" "$F_RESPONSE" "$F_API_MAPPER" "$F_RESOURCE"; do
    echo "  ${f#$ROOT/}"
done
if [ "$ADDED_CONFIG" = true ]; then
    echo
    echo "Appended a placeholder to application.properties — replace REPLACE_ME with the real table id:"
    echo "  ${TABLE_CONFIG}=REPLACE_ME"
fi
echo
echo "Next steps:"
echo "  1. Set ${TABLE_CONFIG} to the real Baserow table id."
echo "  2. Fill in the TODO Schema descriptions in ${ENTITY}Response.java and ${ENTITY}Resource.java."
echo "  3. Adjust @RolesAllowed in ${ENTITY}Resource.java if 'admin, web-users' isn't right."
echo "  4. ./gradlew compileJava"
