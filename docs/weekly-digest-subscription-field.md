# Weekly digest subscription field

The Guests table (id `824729`, already wired via `baserow.guests.guests-table-id`)
needs one new field to support the bot's weekly events digest.

## Field

| Field name          | Baserow field type | Required | Notes                                                                 |
|----------------------|---------------------|----------|------------------------------------------------------------------------|
| `digest_subscribed`  | Boolean             | no       | Opt-out flag for the weekly events digest. `true` or unset/blank = subscribed. Only an explicit `false` opts the guest out. |

No backfill is required: `BaserowWeeklyDigestRepository.findSubscribers()` treats
a `null`/missing value as "subscribed" (`!Boolean.FALSE.equals(digestSubscribed())`),
so existing rows are eligible for the digest as soon as the field exists, without
needing every row set to `true` explicitly.

## API

Both endpoints live under `/events/v1/bot` (`BotResource`), same `@PermitAll`
posture as every other endpoint on that resource — no shared secret is checked,
consistent with `event-notifications/due` etc.

- `GET /events/v1/bot/weekly-digest/subscribers` — guests with a linked Telegram
  chat (`telegram_chat_id` set) who have not explicitly unsubscribed. Response:
  `[{ "chatId": 123456, "guestRowId": 661 }, ...]`.
- `POST /events/v1/bot/weekly-digest/{guestRowId}/unsubscribe` — sets
  `digest_subscribed = false` for that guest row. Called by the bot when a guest
  taps the "Unsubscribe" button on a digest message.
