# "Heard about us" survey fields

The Guests table (id `824729`, already wired via `baserow.guests.guests-table-id`)
needs four new fields to support the bot's "how did you hear about us?" survey, sent
~24h after a guest's row is created.

## Fields

| Field name                | Baserow field type      | Required | Notes |
|----------------------------|--------------------------|----------|-------|
| `created_at`               | Created on (Date)        | yes      | Baserow's built-in row-creation timestamp, enabled and named `created_at` so it's exposed under that API name (same convention as `created_at` on the Registrations table, see `BaserowRegistrationRow`). Used as the anchor for the 24h delay. |
| `heard_about_source`       | Single select            | no       | Options: `Instagram`, `Google`, `Friends`, `Other`. Set from the guest's button tap. |
| `heard_about_comment`      | Long text                 | no       | Optional free-text follow-up comment. |
| `heard_about_survey_sent`  | Boolean                  | no       | Idempotency flag. `true` is patched onto the row the instant it's returned from `/due`, mirroring `event_notifications` result rows' `survey_sent` flag — prevents the survey being sent twice. `null`/missing = not yet sent. |

Existing rows have no backfill requirement: rows created before this field existed
will have `created_at` set retroactively by Baserow once the field is enabled (it
reflects the row's actual creation time), so they become eligible for the survey
according to their real signup date.

## API

Both endpoints live under `/events/v1/bot` (`BotResource`), same `@PermitAll`
posture as every other endpoint on that resource — no shared secret is checked.

- `GET /events/v1/bot/heard-about-source/due` — guests with a linked Telegram chat
  (`telegram_chat_id` set) whose `created_at` is 24h+ in the past, haven't already
  been surveyed (`heard_about_survey_sent` not `true`), and the current time is
  within coworking working hours (weekday 10:00–22:00, weekend 10:00–16:00, Yerevan).
  Each returned guest is immediately marked `heard_about_survey_sent = true`.
  Response: `[{ "chatId": 123456, "guestRowId": 661 }, ...]`.
- `POST /events/v1/bot/heard-about-source/{guestRowId}/answer` body
  `{ "source": "Instagram", "comment": "..." }` — patches `heard_about_source` and
  `heard_about_comment` for that guest row. Called by the bot once the guest has
  answered (button tap + optional free-text comment, or skipped the comment).
