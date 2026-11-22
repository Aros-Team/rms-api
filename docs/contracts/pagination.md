# Pagination Contract

> Source of truth for the shape of every list response in the RMS API.
> Applies to every endpoint that can return more than one item.

## 1. Two shapes, by kind of collection

| Kind | Shape | Examples |
|------|-------|----------|
| **Paged** (the client asks for a page) | `{ "content": [...], "page": { ... } }` | `GET /api/v1/orders`, `GET /api/v1/products`, `GET /api/v1/supplies`, `GET /api/v1/supplies/variants`, `GET /api/v1/payments`, `GET /api/v1/payments/pending`, `GET /api/v1/admin/special-selections/{productId}/history` |
| **Bare list** (no page request) | `[ ... ]` | `GET /api/v1/tables`, `GET /api/v1/categories`, `GET /api/v1/areas`, `GET /api/v1/workers`, `GET /api/v1/schedules`, `GET /api/v1/config` |

Both shapes live inside the success envelope, so the wire response is:

```json
{
  "data": {
    "content": [ { "id": 1 } ],
    "page": { "number": 0, "size": 20, "totalElements": 52, "totalPages": 3 }
  }
}
```

or

```json
{ "data": [ { "id": 1 }, { "id": 2 } ] }
```

## 2. Page metadata

| Field | Type | Meaning |
|-------|------|---------|
| `page.number` | integer | Zero-based page index. |
| `page.size` | integer | Requested page size. |
| `page.totalElements` | integer | Total matching rows. |
| `page.totalPages` | integer | `ceil(totalElements / size)`, `0` when empty. |

This is the Spring Data `Page`/`PagedModel` dialect. It is produced by
`aros.services.rms.infraestructure.common.dto.PageResponse`.

Rules:

- Never mix a top-level `items`/`total`/`total_pages` dialect with the `content`/`page` dialect.
  Legacy clients that need tolerance should normalize both shapes at their boundary.
- `totalPages` is always present and never negative.
- An empty page is `"content": []` with `totalElements = 0`.

## 3. The one documented exception: analytics alerts

`GET /api/v1/analytics/alerts` is offset-based and its contract is published in
`analytics.yaml`, so it keeps its documented shape:

```json
{ "data": { "items": [ ... ], "page": { "limit": 50, "offset": 0, "total": 5 } } }
```

It is the only paged endpoint that does not use `{content, page:{number,size,totalElements,totalPages}}`.
Changing it would break the published analytics schema and the alerts UI.

## 4. Query parameters

| Param | Used by | Meaning |
|-------|---------|---------|
| `page` | all page-number endpoints | Zero-based page index, default `0`. |
| `size` | all page-number endpoints | Page size, default `20`. |
| `limit` | alerts | Max rows, `1..100`, default `50`. |
| `offset` | alerts | Rows to skip, `>= 0`. |

## 5. Contract tests

- `PageResponseTest` pins the record and the derived `totalPages`.
- `OrderQueryControllerWebMvcTest` and `PaymentControllerTest` assert the wire shape
  (`$.data.content`, `$.data.page.totalElements`, `$.data.page.totalPages`).
- `ApiDocsEnvelopeIntegrationTest` fails if a paged endpoint is published without the envelope.
