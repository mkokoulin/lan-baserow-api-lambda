# Event likes Baserow table

Table ID: `1202668` (already wired into `application.properties` and `template.yaml`).

## Fields

| Field name  | Baserow field type | Required | Notes |
|-------------|---------------------|----------|-------|
| `anon_id`   | Single line text (primary field) | yes | Random UUID generated client-side in the visitor's browser (`localStorage`), used to dedupe one like per browser. Not a real user identity. Primary field only because Baserow requires *some* field to be primary and doesn't allow "Link to table" to hold that role — no semantic significance beyond that. |
| `event_id`  | **Link to table** → `events`, single relationship (Allow multiple relationships off) | yes | Which event this like belongs to, by Baserow's internal row id — not the event's external UUID. The backend resolves the event's `external_id` (UUID, what the API and frontend use) to this internal row id via `BaserowEventClient.findUniqueByExternalId` before every like/unlike/count call. Creating this field with "Create related field in linked table" on also adds a reverse "event_likes" column to the `events` table, so organizers can see likes directly against an event row in Baserow. |
| `created_at` | **Created on** (Baserow special field type) | yes | Auto-filled by Baserow on row creation, read-only — do not include it in create requests. Mirrors the `reviews` table's `created_at` field. |

No `is_visible` / moderation field — likes aren't curated content, they're a
raw signal for organizers deciding whether to run an event again.

Note: an earlier version of this table used `event_external_id` as a plain
text UUID column (and a Baserow field literally typed **UUID**, which is a
system-generated, non-writable identifier — not a place to store our own
UUID). Both turned out to be dead ends: the **UUID** field type rejects
writes (400 on create), and even switching it to plain text would have kept
likes invisible from the `events` side. Link-to-table fixes both.

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
