# New resource scaffold

`scripts/new-resource.sh` generates the full read vertical slice for a new
Baserow-backed resource — the same ten files/layers as e.g. `Vacancy`
(`VacancyResource` → `VacancyService` → `VacancyRepository` →
`BaserowVacancyRepository` → `BaserowVacancyClient`). Use it instead of
hand-copying an existing resource when adding a new simple "list + get by id"
endpoint backed by a Baserow table.

## Usage

```bash
scripts/new-resource.sh <Entity> <table.config.property> </api/path> [field:Type[?] ...]
```

- `<Entity>` — PascalCase, singular (`Partner`, not `partners`).
- `<table.config.property>` — the `application.properties` key that will hold
  the Baserow table id, following the existing `baserow.<domain>.<name>-table-id`
  convention (e.g. `baserow.careers.partners-table-id`).
- `</api/path>` — the JAX-RS `@Path` for the resource (e.g. `/careers/v1/partners`).
- `field:Type[?]` — zero or more extra fields beyond the standard `id`/`external_id`.
  Append `?` to make a field nullable.

### Example

```bash
scripts/new-resource.sh Partner baserow.careers.partners-table-id /careers/v1/partners \
    name:String logoUrl:String? websiteUrl:String? rank:Integer
```

### Supported field types

`String`, `Integer`, `Long`, `Boolean`, `Double`, `UUID`. A non-nullable
`String` gets `@NotBlank` on the Baserow row DTO; every other non-nullable
type gets `@NotNull`; anything with a trailing `?` gets `@Nullable` instead.
Baserow column names are derived automatically (`logoUrl` → `logo_url`).

## What it generates

| Layer | File |
|---|---|
| domain model | `src/main/java/com/lan/app/domain/model/<Entity>.java` |
| repository port | `src/main/java/com/lan/app/repository/<Entity>Repository.java` |
| Baserow row DTO | `src/main/java/com/lan/app/infrastructure/baserow/dto/Baserow<Entity>Row.java` |
| Baserow REST client | `src/main/java/com/lan/app/infrastructure/baserow/client/Baserow<Entity>Client.java` |
| Baserow mapper | `src/main/java/com/lan/app/infrastructure/baserow/mapper/Baserow<Entity>Mapper.java` |
| repository adapter | `src/main/java/com/lan/app/infrastructure/baserow/repository/Baserow<Entity>Repository.java` |
| service | `src/main/java/com/lan/app/service/<Entity>Service.java` |
| API response DTO | `src/main/java/com/lan/app/api/dto/response/<Entity>Response.java` |
| API mapper | `src/main/java/com/lan/app/api/mapper/Api<Entity>Mapper.java` |
| JAX-RS resource | `src/main/java/com/lan/app/api/resource/<Entity>Resource.java` |

It also appends a placeholder line to `src/main/resources/application.properties`
if `<table.config.property>` isn't already set there:

```properties
# TODO: set the real Baserow table id for Partner
baserow.careers.partners-table-id=REPLACE_ME
```

The generated resource exposes exactly two endpoints, mirroring the dominant
pattern across the existing resources:

- `GET </api/path>` — list all rows
- `GET </api/path>/{externalId}` — get one row by its external UUID

If a target file already exists, the script aborts before writing anything —
it never overwrites.

## After running it

The script prints these as a reminder, but spelled out:

1. Set the real Baserow table id in `application.properties` (replace
   `REPLACE_ME`).
2. Fill in the `TODO: describe ...` strings in `<Entity>Response.java` and
   `<Entity>Resource.java` (`@Schema` descriptions, the `@Tag` description,
   the two `@Operation` descriptions).
3. Adjust `@RolesAllowed({"admin", "web-users"})` in `<Entity>Resource.java`
   if those aren't the right roles for this endpoint.
4. Add Baserow columns matching the fields you passed (plus `id` and
   `external_id`, which every generated table is expected to have).
5. `./gradlew compileJava` to confirm it builds.

## What it deliberately doesn't do

- **Only generates `list()` + `get(UUID)`.** That's the read pattern used by
  ~20 of the existing resources. Write endpoints (`POST`/`PATCH`/`DELETE`)
  aren't generated — the generated `<Entity>Resource.java` has a `// TODO`
  comment at the bottom showing where to add them, following the same
  resource → service → repository chain.
- **Doesn't pick Baserow column types for you** beyond the Java type you
  specify — you still create the actual columns in Baserow by hand.
- **Doesn't generate tests.** Copy an existing `*RepositoryTest`/resource
  test and adapt it, same as before the script existed.
- **Doesn't wire JWT roles/scopes** beyond the `@RolesAllowed` annotation —
  if the endpoint needs something more specific, edit it by hand.
