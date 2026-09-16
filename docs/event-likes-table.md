# Event likes Baserow table

Table ID: `1202668` (already wired into `application.properties` and `template.yaml`).

## Fields

| Field name          | Baserow field type | Required | Notes |
|----------------------|---------------------|----------|-------|
| `event_external_id`  | Single line text (UUID) | yes | The `external_id` of the row in the `Events` table this like belongs to. Plain text, not a "Link to table" field — nothing addresses a like by its own id, so a link-row lookup would only add an extra Baserow round trip for no benefit. |
| `anon_id`             | Single line text        | yes | Random UUID generated client-side in the visitor's browser (`localStorage`), used to dedupe one like per browser. Not a real user identity — see `docs/faq-table.md` for the general `external_id`-as-UUID convention this table intentionally does *not* follow (this table has no `external_id` of its own). |
| `created_at`          | **Created on** (Baserow special field type) | yes | Auto-filled by Baserow on row creation, read-only — do not include it in create requests. Mirrors the `reviews` table's `created_at` field. |

No `is_visible` / moderation field — likes aren't curated content, they're a
raw signal for organizers deciding whether to run an event again.

## API

Base path: `/events/v1/{externalId}/likes` (same auth as other `/events/v1`
endpoints — `admin` or `web-users` role, called only server-side from the
Next.js site, never directly from the browser).

- `POST /events/v1/{externalId}/likes` — body `{ "anonId": "<uuid>" }`. Idempotent: liking twice with the same `anonId` does not double-count. Returns the current count.
- `DELETE /events/v1/{externalId}/likes?anonId=<uuid>` — removes the like, if any. Returns the current count.

Response shape (`EventLikesResponse`):

```json
{ "count": 12 }
```

`EventResponse` (from `GET /events/v1` and `GET /events/v1/{externalId}`)
also carries the current count as `likesCount`.
