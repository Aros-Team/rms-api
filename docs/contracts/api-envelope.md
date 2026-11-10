# API Response Envelope & Money Contract

> Source of truth for the shape of **every** HTTP response and every monetary value in the RMS API.
> Introduced by Activity 11 (`api-envelope-money-dto-standardization`).
> Companion OpenAPI spec: the generated Swagger document (`/swagger-ui/index.html`) already
> describes this envelope.

## 1. Success responses — one envelope

Every **2xx JSON** response from every controller is wrapped exactly once:

```json
{
  "data": { },
  "meta": { }
}
```

| Field | Type | Present | Meaning |
|-------|------|---------|---------|
| `data` | any | always | The endpoint payload (object, array, or paged body). |
| `meta` | object | only when non-empty | Optional metadata (pagination, tracing, …). Omitted from JSON when null/empty. |

Rules:

- `data` always holds the **original** response body. A list endpoint returns
  `{ "data": [ ... ] }`; a paged endpoint returns `{ "data": { "content": [ ... ], ... } }`.
- `204 No Content` remains body-less. No envelope.
- Binary/image responses are not wrapped.
- `GET /.well-known/jwks.json` (`application/jwk-set+json`) is **not** wrapped; it must remain a
  raw JWK Set for JWT verification.
- The envelope is applied by `ApiEnvelopeAdvice`; controllers keep returning their DTOs.
- The envelope is documented in OpenAPI by `ApiEnvelopeOpenApiCustomizer`.

Example — `GET /api/v1/orders/{id}`:

```json
{
  "data": {
    "id": 42,
    "date": "2026-11-10T14:30:00",
    "status": "QUEUE",
    "tableId": 1,
    "total": { "amount": "37.50", "currency": "COP" },
    "details": []
  }
}
```

## 2. Error responses — RFC 7807, never wrapped

All `4xx` / `5xx` responses are `application/problem+json` documents. They are **not** placed
inside `{ "data": ... }`, because the problem document is already the envelope:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Order not found: 999",
  "instance": "/api/v1/orders/999"
}
```

| Field | Meaning |
|-------|---------|
| `status` | HTTP status code (also the response status). |
| `title` | Short reason phrase for the status. |
| `detail` | Human-readable, specific message. **This replaces the old `message` field.** |
| `type` | Problem type URI (`about:blank` by default). |
| `instance` | Request URI that produced the problem. |

Clients must read `detail` (not `message`) and must not expect a `data` property on errors.

### Auth failures are not bodyless

Filter-level failures use the same problem document. A missing/invalid/expired token returns `401`
with the five fields above **and** the standard `WWW-Authenticate` header (`error="invalid_token"`
when applicable). A denied request returns `403` with the same body shape. Clients can distinguish
missing/invalid credentials from insufficient permissions by status and still read `detail`/`title`.

### OpenAPI is the source of truth

`/v3/api-docs` documents the envelope for **every** 2xx JSON operation, including springdoc's `*/*`
media type for endpoints without an explicit `produces`. `ApiDocsEnvelopeIntegrationTest` fails if
any 2xx JSON response is flat, so the published contract cannot silently drift from runtime.

## 3. Money — one type: `MoneyDto`

Every monetary value in a response uses the shared `MoneyDto`:

```json
{ "amount": "1234.56", "currency": "COP" }
```

| Field | Type | Rules |
|-------|------|-------|
| `amount` | **string** | Decimal string, never a JSON number. Preserves precision. |
| `currency` | string | ISO 4217 code (`COP`). |

Rules:

- `amount` is **always a string**. Do not parse it as a JavaScript `number`; use a decimal
  library or keep it as a display string.
- Applies to prices, costs, salaries, payroll amounts, purchase totals, surcharges, order totals,
  and every other monetary field.
- Percentages, ratios, multipliers, quantities, hours, and stock remain plain numbers
  (e.g. `marginPercent: 30.0`, `quantity: 2.5`).
- Request bodies stay numeric for now; only responses are standardized.
- There is no second money shape. The analytics, payroll, product, purchase, inventory, user,
  special-selection, and order surfaces all use `MoneyDto`.

## 4. Order total

`OrderResponse.total` is computed by the backend:

```json
{
  "id": 42,
  "total": { "amount": "37.50", "currency": "COP" },
  "details": [
    { "unitPrice": { "amount": "12.50", "currency": "COP" } },
    { "unitPrice": { "amount": "25.00", "currency": "COP" } }
  ]
}
```

- `total` = Σ `details[].unitPrice`.
- `unitPrice` **already includes** the per-line `extraCharge`; do not add `extraCharge` again.
- Empty or unpriced orders return `{ "amount": "0.00", "currency": "COP" }`.
- The same object is sent over WebSocket order events; clients can stop recomputing totals.

## 5. Frontend migration checklist

1. Unwrap every success response: `response.data` (or `response.data.data` if the HTTP client also
   wraps). Update services, caches, and specs.
2. Replace numeric money handling with `{ amount, currency }` objects; format `amount` as a decimal
   string. Do not do arithmetic on the string.
3. Replace local order-total calculation (`calculateTotalPrice`) with `order.total`.
4. Replace error `message` reads with RFC 7807 `detail`.
5. Leave JWKS and binary/image handling unchanged.

## 6. Exceptions at a glance

| Surface | Envelope | Money |
|---------|----------|-------|
| All `/api/**` 2xx JSON | `{ data, meta? }` | `MoneyDto` |
| All `/api/**` 4xx/5xx | RFC 7807 `application/problem+json` | n/a |
| `204 No Content` | none (no body) | n/a |
| `/.well-known/jwks.json` | none (raw JWK Set) | n/a |
| Image/binary endpoints | none | n/a |
