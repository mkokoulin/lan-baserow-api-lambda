# FAQ Baserow table

Table ID: `1097501` (already wired into `application.properties` and `template.yaml`).

## Fields

| Field name    | Baserow field type          | Required | Notes                                                            |
|---------------|------------------------------|----------|--------------------------------------------------------------------|
| `external_id` | Single line text / UUID*      | yes      | Stable public identifier, generated once per row (see note below) |
| `question_en` | Single line text              | yes      | Question, English                                                 |
| `question_ru` | Single line text              | yes      | Question, Russian                                                 |
| `answer_en`   | Long text, **rich text ON**    | yes      | Answer, English. HTML produced by Baserow's rich text editor       |
| `answer_ru`   | Long text, **rich text ON**    | yes      | Answer, Russian. HTML produced by Baserow's rich text editor       |
| `position`    | Number (integer)              | no       | Sort order, ascending. Leave blank to fall back to row order      |
| `is_visible`  | Boolean                       | yes      | Only rows with `is_visible = true` are returned by the API        |

\* Baserow has no native UUID field type. Use a single line text field named
`external_id` and fill it with a UUID v4 per row (e.g. generate with
`uuidgen` / `crypto.randomUUID()`). This mirrors the `Initiatives`,
`Vacancies`, and `Events` tables already in use — copy the same convention.

## Field-level tips in Baserow UI

- For `answer_en` / `answer_ru`: create as **Long text**, then enable
  **"Rich text formatting"** in the field's settings so editors get a
  WYSIWYG toolbar (bold, links, lists, etc.) — Baserow stores the result as
  sanitized HTML, which is exactly what the API returns as-is.
- `is_visible` lets you draft/hide FAQ entries without deleting them.
- Sort the Baserow grid view by `position` ascending so editors see the
  same order visitors will see on the site.

## Example rows

| external_id                          | question_en                    | question_ru                          | answer_en                                             | answer_ru                                                     | position | is_visible |
|---------------------------------------|---------------------------------|----------------------------------------|--------------------------------------------------------|-----------------------------------------------------------------|----------|------------|
| 8f14e45f-ceea-4a9e-8c96-1b1f3c0a7e11  | What are your working hours?   | Какой у вас график работы?           | `<p>We are open daily from <strong>9:00 to 22:00</strong>.</p>` | `<p>Мы работаем ежедневно с <strong>9:00 до 22:00</strong>.</p>` | 1        | true       |
| 3c2a2e5b-6f1d-4b8a-9e2a-2b6a1d4f9c02  | How do I book a meeting room?  | Как забронировать переговорную?       | `<p>Book via the <a href="/coworking/booking">booking page</a> or ask at reception.</p>` | `<p>Забронируйте на <a href="/coworking/booking">странице бронирования</a> или на ресепшене.</p>` | 2        | true       |

## API

Base URL: `/coworking/v1/faq` (same auth as `/coworking/v1/initiatives` —
requires `admin` or `web-users` role).

- `GET /coworking/v1/faq` — list all visible FAQ entries, ordered by `position`.
- `GET /coworking/v1/faq/{externalId}` — get a single entry by its UUID.

Response shape (`FaqResponse`):

```json
{
  "id": "8f14e45f-ceea-4a9e-8c96-1b1f3c0a7e11",
  "questionEn": "What are your working hours?",
  "questionRu": "Какой у вас график работы?",
  "answerEn": "<p>We are open daily from <strong>9:00 to 22:00</strong>.</p>",
  "answerRu": "<p>Мы работаем ежедневно с <strong>9:00 до 22:00</strong>.</p>",
  "position": 1
}
```
