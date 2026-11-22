# Analytics API — Contract for Frontend

> Source of truth for the `/api/v1/analytics/**` surface.
> The companion OpenAPI spec lives at `docs/contracts/analytics.yaml`.
> Generated Swagger UI: <http://localhost:8080/swagger-ui/index.html#/Analytics>.

## 1. Scope

Five analytical modules, all read-only, all admin-only:

| # | Module | Path prefix | Backing aggregate |
|---|--------|-------------|-------------------|
| 1 | Prime Cost & Margins | `/prime-cost` | `monthly_financial_summary` (rolled up from daily) |
| 2 | Menu Engineering (BCG) | `/menu-engineering` | `menu_performance_cache` (invalidated on price/recipe change) |
| 3 | Operational Efficiency (RevPASH & Turns) | `/operations` | Live + daily rollups |
| 4 | Customer Cohort | `/cohort` | Live (window functions over `orders`) |
| 5 | Variance Alerts | `/alerts` | `system_alerts` (populated by nightly job) |

## 2. Auth

- **Required**: `Authorization: Bearer <jwt>`
- **Role**: `ROLE_ADMIN` only. Any other authenticated role → `403 Forbidden`.
- Token validation: standard JWT chain (`/.well-known/jwks.json`).
- Errors follow RFC 7807 `application/problem+json`.

## 3. Time buckets

Four buckets supported everywhere a period is accepted:

| `bucket` | Key format | Example |
|----------|------------|---------|
| `daily`   | `YYYY-MM-DD` | `2026-07-17` |
| `weekly`  | `YYYY-Www` (ISO week) | `2026-W29` |
| `monthly` | `YYYY-MM` | `2026-07` |
| `yearly`  | `YYYY` | `2026` |

### Query pattern

Two equivalent ways to ask for a range:

```http
# (A) Range form — preferred for charts
GET /api/v1/analytics/prime-cost?bucket=monthly&from=2026-01&to=2026-07

# (B) Point form — single period
GET /api/v1/analytics/prime-cost?bucket=monthly&year=2026&month=7
```

| Param | Required | Notes |
|-------|----------|-------|
| `bucket` | yes | `daily` \| `weekly` \| `monthly` \| `yearly` |
| `from`   | yes (A) | inclusive, format must match bucket |
| `to`     | yes (A) | inclusive, must be `>= from` |
| `year`   | yes (B) | 4 digits |
| `month`  | only for `monthly` (B) | 1–12 |
| `week`   | only for `weekly` (B)  | 1–53 (ISO) |
| `day`    | only for `daily` (B)   | 1–31 |
| `categoryId` | optional, menu-engineering only | filter menu items by product category |
| `limit`, `offset` | optional, alerts only | paging, max `limit=100` |

### Validation

| Failure | HTTP | `type` URI |
|---------|------|-----------|
| Bad date format for bucket | 400 | `/analytics/invalid-period` |
| `to < from` | 422 | `/analytics/invalid-range` |
| Range > 366 days | 422 | `/analytics/range-too-large` |

## 4. Money

Analytics uses the shared API money contract (see `docs/contracts/api-envelope.md`):

```json
{ "amount": "1234.56", "currency": "COP" }
```

`amount` is always a decimal **string**; `currency` is an ISO 4217 code. All monetary fields in
responses use this wrapper — never raw floats or numbers. Percentages (`pct`, `marginPercent`)
remain numeric.

All 2xx JSON envelopes in this document are additionally wrapped as `{ "data": ..., "meta": ... }`
and errors are RFC 7807 `application/problem+json`. See `docs/contracts/api-envelope.md`.

## 5. Module contracts

### 5.1 Prime Cost (`GET /api/v1/analytics/prime-cost`)

**Math** (per user's spec):
```
COGS_category   = starting_inventory + purchases − ending_inventory       (per category)
labor_area      = gross_wages + employer_taxes + benefits                  (per area FOH/BOH)
prime_cost      = Σ COGS_category (food + beverage + alcohol + other) + Σ labor_area (FOH + BOH + other)
net_sales       = gross_sales − discounts − comped
prime_cost_pct  = prime_cost / net_sales
```

**Response shape** (`PrimeCostReport`):

```jsonc
{
  "period": { "bucket": "monthly", "from": "2026-01", "to": "2026-07", "keys": ["2026-01", ..., "2026-07"] },
  "series": [
    {
      "key": "2026-07",
      "netSales":   { "amount": "125000000.00", "currency": "COP" },
      "grossSales": { "amount": "138000000.00", "currency": "COP" },
      "discounts":  { "amount": "8000000.00",   "currency": "COP" },
      "comped":     { "amount": "5000000.00",   "currency": "COP" },
      "cogs": {
        "total":  { "amount": "42000000.00", "currency": "COP" },
        "byCategory": [
          { "category": "FOOD",     "amount": { "amount": "30000000.00", "currency": "COP" }, "pct": 71.43 },
          { "category": "BEVERAGE", "amount": { "amount": " 8000000.00", "currency": "COP" }, "pct": 19.05 },
          { "category": "ALCOHOL",   "amount": { "amount": " 4000000.00", "currency": "COP" }, "pct":  9.52 }
        ]
      },
      "labor": {
        "total":  { "amount": "35000000.00", "currency": "COP" },
        "byArea": [
          { "area": "FOH", "amount": { "amount": "18000000.00", "currency": "COP" }, "pct": 51.43 },
          { "area": "BOH", "amount": { "amount": "17000000.00", "currency": "COP" }, "pct": 48.57 }
        ]
      },
      "primeCost":    { "amount": "77000000.00", "currency": "COP" },
      "primeCostPct": 61.60,
      "margins": {
        "grossProfitPct": 66.40,
        "netProfitPct":   4.20
      },
      "dataCompleteness": "FULL"   // FULL | PARTIAL | EMPTY
    }
  ],
  "dataCompleteness": "PARTIAL",
  "notes": [
    "Inventory snapshots missing — COGS approximated via FIFO over inventory_movements."
  ]
}
```

**Performance**: pre-aggregated daily rollups → `monthly_financial_summary` table.
`dataCompleteness` flags ranges where source data is missing.

**Buckets**: `daily`, `weekly`, `monthly` and `yearly` are all served from the same table.
`POST /api/v1/analytics/refresh` backfills the derived rows; the nightly job also rolls
yesterday's daily row into its week, month and year buckets. The daily-to-higher rollup sums
COGS, labor, discounts and comped from the daily rows and recomputes every percentage.

### 5.2 Menu Engineering (`GET /api/v1/analytics/menu-engineering`)

**Math**:
```
For each active SKU:
  units_sold      = Σ quantity over period
  revenue         = Σ (quantity × sell_price)
  recipe_cost     = product.recipe.total_cost (Money value object)
  avg_option_cost = average cost of chosen options per order
  effective_cost  = recipe_cost + avg_option_cost
  gp_per_unit     = sell_price − effective_cost
  contribution    = units_sold × gp_per_unit
  median_volume   = median(units_sold across SKUs)
  median_margin   = median(gp_per_unit across SKUs)

Quadrant:
  STAR       = units >= median_volume AND gp >= median_margin
  PLOWHORSE  = units >= median_volume AND gp <  median_margin
  PUZZLE     = units <  median_volume AND gp >= median_margin
  DOG        = units <  median_volume AND gp <  median_margin
```

**Response shape** (`MenuEngineeringReport`):

```jsonc
{
  "period": { "bucket": "monthly", "from": "2026-07", "to": "2026-07", "keys": ["2026-07"] },
  "median": { "volume": 142, "margin": { "amount": "8500.00", "currency": "COP" } },
  "items": [
    {
      "productId": 42,
      "productName": "Lomo en salsa",
      "categoryId": 3,
      "categoryName": "Platos fuertes",
      "unitsSold": 320,
      "revenue":   { "amount": "9600000.00", "currency": "COP" },
      "recipeCost":{ "amount": "3200000.00", "currency": "COP" },
      "avgOptionCost":    { "amount": "640000.00",  "currency": "COP" },
      "effectiveCost":    { "amount": "3840000.00", "currency": "COP" },
      "grossProfitPerUnit": { "amount": "20000.00", "currency": "COP" },
      "totalContribution":  { "amount": "6400000.00", "currency": "COP" },
      "quadrant": "STAR"
    }
  ],
  "cacheStatus": {
    "lastRefreshedAt": "2026-07-17T02:00:00Z",
    "sourceVersion": "v17",
    "ttlSeconds": 86400
  },
  "dataCompleteness": "FULL"
}
```

**Cache**: `menu_performance_cache` table. Invalidated on:
- `products.base_price` change
- any row in `product_recipes` change

### 5.3 Operational Efficiency (`GET /api/v1/analytics/operations`)

**Definitions** (all over the requested period, `CANCELLED` orders excluded):

```
covers                    = Σ COALESCE(party_size, 1) over delivered orders
occupied_minutes          = Σ (close_time − open_time) for orders with both timestamps
occupied_seat_minutes     = Σ occupied_minutes × covers
table_count               = COUNT(*) from tables
seat_count                = Σ capacity from tables
days                      = calendar days in [from, to]
operating_minutes_per_day = default_close − default_open (analytics_config)
available_seat_hours      = seat_count × operating_minutes_per_day × days / 60
RevPASH                   = net_sales / available_seat_hours
turns                     = covers / table_count             (overall and per day part)
avg_occupancy_minutes     = occupied_minutes / orders_with_timestamps
labor_share_pct           = labor_total / net_sales × 100    (daily rollups)
```

Day parts come from `analytics_config`: `LUNCH` = `[lunch_start, lunch_end)`, `DINNER` =
`[dinner_start, dinner_end)`, everything else `OTHER`. `dataCompleteness` is `PARTIAL` when some
orders lack `open_time`/`close_time`; those orders are excluded from occupancy metrics only.

**Golden G3** (2027-01-15, 5 tables × 4 seats, 10 orders, 28 covers, 500 occupied minutes,
1,390 seat-minutes, net sales 1,700,000): `availableSeatHours=240.00`,
`revPash.value=7083.33`, `turns=5.60`, lunch turns `2.40`, dinner turns `3.20`,
`avgOccupancyMinutes=50.00`.

### 5.4 Customer Cohort (`GET /api/v1/analytics/cohort`)

**Identity**: there is still no `customers` table. A visit is an order and its fingerprint is:

```
fingerprint = customer_fingerprint                        # SHA-256 of the normalized phone
            ?? SHA2(customer_phone, 256)                  # when only the phone is stored
            ?? "table:<table_id>"                         # fallback, flagged PARTIAL
```

**Window**: `cohortWindowDays = 90`. An order is **recurring** when the same fingerprint was
already seen in the 90 days before that visit; the remaining orders are **new**. The report also
returns one `cohorts[]` row per bucket key so the UI can render a retention table.

`fingerprintStrategy` is `PHONE` when every order in range carries a phone, `TABLE_ONLY` when none
does, and `PHONE_OR_TABLE` in between. `dataCompleteness` is `PARTIAL` when the table fallback is
used, because the recurring rate is then an upper bound (the same table serves different parties).

**Golden G4** (2027-01: 30 orders, 18 new fingerprints, 12 recurring, 60 covers, 20 phones):
`newClients=18`, `recurringClients=12`, `recurringRatePct=40.00`,
`averageTicketPerOrder=50000.00`, `averageTicketPerCover=25000.00`,
`fingerprintStrategy=PHONE_OR_TABLE`, `dataCompleteness=PARTIAL`.

### 5.5 Variance Alerts (`GET /api/v1/analytics/alerts`)

Rules, thresholds and severity (thresholds come from `analytics_config`):

| Type | Rule | Threshold | Severity |
|------|------|-----------|----------|
| `FOOD_COST_DEVIATION` | food cost % − 12-month moving average | > `food_cost_deviation_pp` pp | RED |
| `LABOR_COST_DEVIATION` | labor cost % − 12-month moving average | > `labor_cost_deviation_pp` pp | YELLOW |
| `REVPASH_DROP` | current week RevPASH vs 4-week moving average | drop > 15% | RED |
| `SALES_DROP_YOY` | orders vs same month last year | drop > `sales_drop_yoy_pct` | YELLOW |
| `LOW_MARGIN_SKU_SPIKE` | DOG SKU units week over week | increase > 50% | YELLOW |

Generation runs daily at 04:00 and on demand through `POST /api/v1/analytics/refresh`. Rows are
deduplicated on `(type, metric, period_key)`, so repeated runs never duplicate an alert.
`PATCH /alerts/{id}/read` is idempotent and returns `204`.

**Golden G5**: 4 open alerts — RED `FOOD_COST_DEVIATION` (+9.00%), RED `REVPASH_DROP` (−25.00%),
YELLOW `SALES_DROP_YOY` (−14.00%), YELLOW `LOW_MARGIN_SKU_SPIKE` (+60.00%).

### 5.6 On-demand refresh (`POST /api/v1/analytics/refresh`)

Rebuilds the menu engineering cache for the trailing months, weeks and days, rolls the daily prime
cost rows up and re-evaluates the alert rules. The development profile also performs a boot-time
backfill when the cache or alert table is empty (`app.analytics.backfill-on-boot=true`).

The menu engineering `cacheStatus.lastRefreshedAt` is the real `MAX(updated_at)` of
`menu_performance_cache`; it is `null` only when the cache is empty.

### 5.7 Top-selling products (`GET /api/v1/products/top-selling`)

Admin-only dashboard endpoint (D-014). Query params: `from` and `to` (ISO dates, default last 30
days) and `limit` (1..100, default 10). Returns products ranked by units sold with `unitsSold` and
`revenue` as `Money`. `CANCELLED` orders are excluded.

## 6. Cross-cutting error model

RFC 7807 `application/problem+json`:

```json
{
  "type": "/analytics/invalid-period",
  "title": "Invalid period",
  "status": 400,
  "detail": "bucket=monthly but 'from' is not YYYY-MM",
  "instance": "/api/v1/analytics/prime-cost",
  "errors": [
    { "parameter": "from", "value": "2026-1", "expected": "YYYY-MM" }
  ]
}
```

Status code map (single source):

| Status | Type URI |
|--------|----------|
| 400 | `/analytics/invalid-period`, `/analytics/invalid-parameter` |
| 401 | `/auth/unauthorized` |
| 403 | `/auth/forbidden` |
| 404 | `/analytics/alert-not-found` |
| 422 | `/analytics/invalid-range`, `/analytics/range-too-large` |
| 500 | `/internal` |

## 7. Data-source assumptions & fallback strategy

Per user's instruction *"use what we have now; only create tables if the business model needs them"*:

| Module | Source today | Action if missing |
|--------|--------------|-------------------|
| Prime Cost — COGS | `inventory_movements`, `purchase_orders`, `inventory_stock` | If no snapshot table → approximate FIFO; flag `dataCompleteness=PARTIAL` |
| Prime Cost — Labor | `schedules` + `time_logs` (V22) | Need hourly rate per user. Add `users.hourly_rate` column in A6 (business-essential). Fallback: skip labor row, flag note |
| Menu Engineering | `order_details`, `products`, `product_recipes` | OK today; cache invalidation needs hooks on price/recipe change |
| Operations | `tables`, `orders` | Need `open_time`, `close_time` on orders, `seat_count` on tables (verify), `analytics_config` for hours |
| Cohort | `orders` only | "Client = order" proxy. No new table |
| Alerts | All aggregates above | Need 30+ days history for MA to work; cold-start flagged |

Tables/columns that **are required** by the business model and will be created in follow-up activities:

1. `users.hourly_rate DECIMAL(10,2)` — labor cost needs a rate per worker.
2. `orders.open_time TIMESTAMP`, `orders.close_time TIMESTAMP` — RevPASH math.
3. `analytics_config` singleton table — operating hours per day-of-week.

Tables/columns we **do NOT** create per user instruction: `customers`, `customer_id` on orders.

## 8. Versioning

- All paths under `/api/v1/analytics/**`.
- Breaking changes → `/api/v2/analytics/**`.
- New query params on existing endpoints = additive, non-breaking.

## 9. Pagination & rate limits

- Alerts: `limit` ≤ 100, `offset` ≥ 0. Default `limit=50`.
- Other endpoints: not paginated; if range > 366 days → 422.
- Internal rate limit: 60 req/min per admin (subject to evolve).

## 10. Frontend integration recipe

```bash
# Generate TS types from the contract
npx openapi-typescript docs/contracts/analytics.yaml -o src/api/analytics-schema.ts

# Mock server for parallel FE work
npx prism mock docs/contracts/analytics.yaml --port 4011
```

FE team can start consuming the contract without waiting for backend completion.