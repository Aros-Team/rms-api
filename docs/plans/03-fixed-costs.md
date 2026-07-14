# Plan 03 — Fixed Costs Module

> New bounded context: `fixedcosts`. Captures monthly operating expenses (rent, utilities, admin salaries, marketing, other) so the dish cost formula can charge each plate its share.

---

## 1. Scope

Allow the admin to register the **total fixed operating expenses of a given calendar month**. The backend:

- Stores one row per `(year, month)` per tenant (single-tenant today; design leaves room for multi-tenant later).
- Computes and exposes `totalFixedCost` as the sum of all categories.
- Exposes a CRUD API for the FE.
- Provides a `FindFixedCostForMonthUseCase` consumed by:
  - `CalculateDishCostUseCase` (Plan 04) for on-the-fly cost.
  - `MonthlyClosingJob` (Plan 05) for snapshot creation.

### Out of scope

- Variable costs (raw material consumption per dish) — Plan 01 + 04.
- Cost allocation per dish — Plan 04.
- Historical fixed cost snapshots — derived live; immutable past values live in `historical_product_costs` (Plan 05).

---

## 2. Database schema

```sql
-- V32__create_monthly_fixed_costs.sql

CREATE TABLE monthly_fixed_costs (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_year       SMALLINT      NOT NULL,                 -- e.g. 2026
    period_month      TINYINT       NOT NULL,                 -- 1..12
    rent              DECIMAL(12,2) NOT NULL DEFAULT 0,
    utilities         DECIMAL(12,2) NOT NULL DEFAULT 0,
    admin_salaries    DECIMAL(12,2) NOT NULL DEFAULT 0,
    marketing         DECIMAL(12,2) NOT NULL DEFAULT 0,
    other_expenses    DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_fixed_cost  DECIMAL(12,2) NOT NULL DEFAULT 0,     -- GENERATED? computed by app
    notes             VARCHAR(500),
    registered_by     BIGINT        NOT NULL,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_period (period_year, period_month),
    CONSTRAINT fk_mfc_user FOREIGN KEY (registered_by) REFERENCES users(id),
    CONSTRAINT chk_mfc_month CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT chk_mfc_year  CHECK (period_year BETWEEN 2000 AND 2100),
    CONSTRAINT chk_mfc_nonneg CHECK (
        rent >= 0 AND utilities >= 0 AND admin_salaries >= 0
        AND marketing >= 0 AND other_expenses >= 0
    )
);

CREATE INDEX idx_mfc_period ON monthly_fixed_costs(period_year, period_month);
```

> **Domain-side note**: per Plan 06, every `DECIMAL(12,2)` here is wrapped as `Money` in domain. The column types stay the same.

### 2.1 Why `total_fixed_cost` is a column, not a generated expr

MySQL generated columns can compute sums but writing through the application layer (after `@Valid`) lets us assert non-negativity per field in one place and keeps the math identical to the response payload. We recompute on every save.

---

## 3. Domain layer

```
core/fixedcosts/
├── domain/
│   ├── FixedCost.java                  // immutable record
│   ├── FixedCostBreakdown.java         // value object holding each category
│   ├── YearMonth.java                  // value object (year, month) with validation
│   ├── exception/
│   │   ├── FixedCostAlreadyExistsException.java   → 409
│   │   ├── FixedCostNotFoundException.java       → 404
│   │   └── InvalidPeriodException.java           → 400
│   └── port/
│       ├── input/
│       │   ├── RegisterFixedCostUseCase.java
│       │   ├── UpdateFixedCostUseCase.java
│       │   ├── GetFixedCostUseCase.java
│       │   ├── ListFixedCostsUseCase.java
│       │   └── DeleteFixedCostUseCase.java
│       └── output/
│           └── FixedCostRepositoryPort.java
```

```java
public record FixedCost(
    Long id,
    YearMonth period,
    FixedCostBreakdown breakdown,
    Money totalFixedCost,
    String notes,
    Long registeredBy,
    Instant createdAt,
    Instant updatedAt,
    Long version,            // @Version mirror — JPA-managed
    Instant closedAt,        // non-null once monthly closing (Plan 05) freezes the row
    Long closingRunId
) {
  public boolean isClosed() { return closedAt != null; }
}

public record FixedCostBreakdown(
    Money rent,
    Money utilities,
    Money adminSalaries,
    Money marketing,
    Money otherExpenses
) {
  public Money total() {
    return rent.plus(utilities).plus(adminSalaries).plus(marketing).plus(otherExpenses);
  }
}

public record YearMonth(int year, int month) {
  public YearMonth {
    if (month < 1 || month > 12)  throw new InvalidPeriodException(...);
    if (year  < 2000 || year > 2100) throw new InvalidPeriodException(...);
  }
  public static YearMonth of(LocalDate d) { return new YearMonth(d.getYear(), d.getMonthValue()); }
  public static YearMonth of(int y, int m) { return new YearMonth(y, m); }
  public LocalDate firstDay() { return LocalDate.of(year, month, 1); }
  public YearMonth previousMonth() {
    return month == 1 ? new YearMonth(year - 1, 12) : new YearMonth(year, month - 1);
  }
}
```

> **BigDecimal discipline**: every monetary field is `Money`. The mapper translates `Money.amount()` (`BigDecimal` scale 2, `HALF_UP`) to the DB column. Arithmetic goes through `Money.plus(...)` — never raw `BigDecimal.add(...)`. See Plan 06 for the full value-object contract.

---

## 4. Application layer (use cases)

| Use case | Verb | Notes | Audit |
|---|---|---|---|
| `RegisterFixedCostService` | `register(cmd)` | Computes `totalFixedCost = sum(breakdown)`; UNIQUE `(year, month)` enforced at DB; → 409 if duplicate | yes — `FIXED_COST_REGISTERED` (full breakdown) |
| `UpdateFixedCostService` | `update(id, cmd)` | Only the **current** open period may be edited; past periods are read-only (closing froze them via Plan 05) | **yes — `FIXED_COST_UPDATED` with old/new breakdown** |
| `GetFixedCostService` | `findByPeriod(period)` | Returns Optional; consumers (Plan 04, 05) treat empty as `Money.zero()` | no (read-only) |
| `ListFixedCostsService` | `findAll(pageable)` | DESC by `(year, month)` | no (read-only) |
| `DeleteFixedCostService` | `delete(id)` | Soft delete only if no `historical_product_costs` references the period (Plan 05) | yes — `FIXED_COST_DELETED` |

### 4.1 Command DTOs

```java
public record RegisterFixedCostCommand(
    int year,
    int month,
    Money rent,
    Money utilities,
    Money adminSalaries,
    Money marketing,
    Money otherExpenses,
    String notes,
    Long performedBy
) {}

public record UpdateFixedCostCommand(
    Long id,
    Money rent,
    Money utilities,
    Money adminSalaries,
    Money marketing,
    Money otherExpenses,
    String notes,
    Long performedBy
) {}
```

`UpdateFixedCostService` rejects edits to a period that already has a `historical_product_costs` snapshot → throws `FixedCostImmutableException` → 409. Reason: the monthly closing process has frozen the values.

---

## 5. REST API surface

Base path: `/api/v1/fixed-costs`. `@Tag(name = "Fixed Costs")`.

| Method | Path | Purpose | Status |
|---|---|---|---|
| `POST` | `/fixed-costs` | Register a period | 201 / 400 / 409 |
| `GET`  | `/fixed-costs` | List paginated (desc by year/month) | 200 |
| `GET`  | `/fixed-costs/{year}/{month}` | Get one period | 200 / 404 |
| `PATCH`| `/fixed-costs/{id}` | Update (current period only) | 200 / 400 / 404 / 409 |
| `DELETE`| `/fixed-costs/{id}` | Soft delete (no historical snapshots) | 204 / 404 / 409 |

### Request / response

```json
// POST /fixed-costs
{
  "year": 2026,
  "month": 7,
  "rent":            { "amount": 3500000.00, "currency": "COP" },
  "utilities":       { "amount":  900000.00, "currency": "COP" },
  "adminSalaries":   { "amount": 4200000.00, "currency": "COP" },
  "marketing":       { "amount":  300000.00, "currency": "COP" },
  "otherExpenses":   { "amount":  150000.00, "currency": "COP" },
  "notes": "Julio 2026"
}

// 201 Created
{
  "id": 8,
  "period": { "year": 2026, "month": 7 },
  "breakdown": {
    "rent":          { "amount": 3500000.00, "currency": "COP" },
    "utilities":     { "amount":  900000.00, "currency": "COP" },
    "adminSalaries": { "amount": 4200000.00, "currency": "COP" },
    "marketing":     { "amount":  300000.00, "currency": "COP" },
    "otherExpenses": { "amount":  150000.00, "currency": "COP" }
  },
  "totalFixedCost":  { "amount": 9050000.00, "currency": "COP" },
  "notes": "Julio 2026",
  "registeredBy": 1,
  "createdAt": "2026-07-14T10:00:00Z",
  "updatedAt": "2026-07-14T10:00:00Z"
}
```

(Per Plan 06, the wire format for money is `{ amount, currency }`.)

---

## 6. Concurrency / locking

- **Pessimistic + optimistic**: `PESSIMISTIC_WRITE` lock on update/delete **plus** `@Version` on the entity so any race that slips past the lock surfaces as `ObjectOptimisticLockingFailureException`.
- Lock timeout hint: `jakarta.persistence.lock.timeout = 3000` (3 s) so a stuck row fails fast.
- Two simultaneous POSTs for the same `(year, month)` → second fails at DB UNIQUE constraint → mapper catches `DataIntegrityViolationException` and rethrows `FixedCostAlreadyExistsException` → 409.
- Update endpoint relies on `@Retryable(ObjectOptimisticLockingFailureException, maxAttempts = 3, backoff = 200 ms × 2.0)`.

---

## 7. Edge cases

| Case | Handling |
|---|---|
| Edit a period with historical snapshots | `FixedCostImmutableException` 409 (Plan 05 froze it) |
| Delete a period that downstream snapshots reference | Reject with 409 + link to snapshot count |
| Sum of breakdown is zero | Allowed (e.g., a closed month with no expenses — useful for testing) |
| Negative field | DB CHECK + service-layer `Money.isNegative()` rejection → 400 |
| Period in the future | Allowed (admin may pre-fill); no constraint |
| Period from before 2000 or after 2100 | `InvalidPeriodException` → 400 |

---

## 8. Tests

| Test class | Coverage |
|---|---|
| `YearMonthTest` | Invalid month/year, previousMonth math, equality |
| `FixedCostBreakdownTest` | `total()` sums all five categories; rejects negative |
| `RegisterFixedCostServiceTest` | Computes total; rejects duplicate `(year, month)`; persists |
| `UpdateFixedCostServiceTest` | Edits allowed only on current period; rejects immutable period |
| `DeleteFixedCostServiceTest` | Soft delete + reject when snapshots exist |
| `FixedCostControllerIT` | Full happy path + 409 + 404 |

---

## 9. Acceptance criteria

- [ ] `V32` migration creates `monthly_fixed_costs` with constraints and unique index.
- [ ] `V32_1` migration adds `version BIGINT NOT NULL DEFAULT 0` and `closed_at`/`closing_run_id` columns.
- [ ] Entity carries `@Version`; update/delete ports use `@Lock(PESSIMISTIC_WRITE)` + 3 s timeout hint.
- [ ] Five CRUD endpoints documented in § 5 implemented with `@Tag` / `@Operation` / `@ApiResponse`.
- [ ] `total_fixed_cost` always equals `sum(breakdown)` — covered by a service test that mutates every field using `Money.plus(...)`.
- [ ] Past-period immutability enforced **after** a `historical_product_costs` snapshot exists for that period (Plan 05 wires this).
- [ ] Money values use `Money` value object (Plan 06).
- [ ] `@Retryable` on optimistic-lock failure exercised by concurrency test.
- [ ] `./harness/harness.sh` exits 0.

---

## 10. Risks / follow-ups

- **Currency conversion**: if multi-currency ever lands, the breakdown fields will need a per-row currency. Defer.
- **Multi-tenant**: today single-tenant; a `tenant_id` column can be added in a later migration without breaking the unique constraint if scoped to `(tenant_id, year, month)`.
- **Re-opening a closed period**: out of scope; if business needs it, add an "amendment" use case that writes to a separate audit table rather than mutating the historical snapshot.