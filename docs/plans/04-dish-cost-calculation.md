# Plan 04 — Product / Dish Cost Calculation (Core Formula)

> Bounded context: `cost` (new), extending `product`, `inventory`, `user`, and `fixedcosts`. Delivers the **Total Dish Cost = Materials + Labor + Proportional Fixed Costs** formula with on-the-fly evaluation.

---

## 1. Scope

Given a `productId` and a target `(year, month)`:

```
Total Dish Cost = RawMaterialCost + LaborCost + ProportionateFixedCost
```

Where:

```
RawMaterialCost   = Σ ( recipe.quantity × supply_variant.unit_cost )  over all variants in recipe
LaborCost         = ( preparation_time_in_minutes / 60 ) × labor_cost_per_hour
ProportionateFixedCost = totalFixedCost(month) / totalDishesSold(month)
```

### In scope

- New bounded context `cost` with a `DishCost` value object.
- `CalculateDishCostUseCase` orchestrating the formula above.
- New dependency: `system_configuration` global settings table (specifically `cost_per_hour_of_cooking_labor`, `default_supply_category_id`).
- Reuse:
  - `product_recipes` (already exists) → material cost.
  - `orders` table (date filter) → dish count.
  - `monthly_fixed_costs` (Plan 03) → fixed cost input.
  - `Salary` / `User` (existing) — alternative labor cost (admin/avg salary per hour); see § 6.2.

### Out of scope

- Historical frozen cost (snapshots) → Plan 05.
- Price suggestion / margin tools → `SpecialSelectionPricingService` (existing).
- Tax / VAT calculation.

---

## 2. Database schema (delta)

### 2.1 New `system_configuration` table

```sql
-- V33__create_system_configuration.sql
CREATE TABLE system_configuration (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    `key`     VARCHAR(120) NOT NULL UNIQUE,
    value     VARCHAR(500) NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sc_user FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Seed defaults
INSERT INTO system_configuration (`key`, value) VALUES
    ('default_currency',           'COP'),
    ('default_supply_category_id', '1'),     -- update if seed IDs differ
    ('cost_per_hour_of_cooking_labor', '12000.00'),  -- COP per hour
    ('business_timezone',          'America/Bogota'),
    ('monthly_closing_day',        '1'),     -- day of month the cron runs
    ('monthly_closing_hour',       '02');    -- 02:00 local
```

Why a key-value table:
- Cheap to evolve (no migration per new global setting).
- One row per global flag.
- Auditable via `updated_by` / `updated_at`.

### 2.2 Extend `products`

```sql
-- V34__add_prep_minutes_to_products.sql
ALTER TABLE products
    ADD COLUMN estimated_prep_minutes INT NULL,
    ADD CONSTRAINT chk_prep_minutes_nonneg CHECK (estimated_prep_minutes IS NULL OR estimated_prep_minutes >= 0);
```

(Aligns with in-progress activity `on-the-fly-cost-calculation` in `activities.json`.)

---

## 3. Domain layer

```
core/cost/
├── domain/
│   ├── DishCost.java               // immutable value object
│   ├── DishCostBreakdown.java      // itemized lines (per-variant contributions)
│   ├── DishCostContext.java        // (year, month, dishesSold, fixedCost)
│   ├── exception/
│   │   ├── ProductNotFoundException.java         → 404
│   │   ├── ProductHasNoRecipeException.java      → 422
│   │   ├── MissingLaborCostConfigurationException.java → 422
│   │   └── MissingFixedCostConfigurationException.java → 422
│   ├── port/
│   │   ├── input/
│   │   │   └── CalculateDishCostUseCase.java
│   │   └── output/
│   │       ├── SystemConfigurationRepositoryPort.java
│   │       ├── DishSalesCountRepositoryPort.java     // counts orders by month
│   │       ├── FixedCostQueryPort.java               // Plan 03 lookup
│   │       └── ProductRecipeQueryPort.java           // existing
│   └── util/
│       └── DishCostFormula.java    // pure math helpers, framework-free
└── application/
    └── service/
        └── CalculateDishCostService.java
```

```java
public record DishCost(
    Long productId,
    String productName,
    YearMonth period,
    Money materialCost,
    Money laborCost,
    Money fixedCostAllocation,
    Money totalCost,
    DishCostBreakdown breakdown,
    Instant computedAt
) {}

public record DishCostBreakdown(
    List<MaterialLine> materials,
    LaborLine labor,
    FixedCostLine fixedCost
) {}

public record MaterialLine(
    Long supplyVariantId,
    String supplyName,
    BigDecimal quantity,
    Money unitCost,
    Money lineCost
) {}

public record LaborLine(
    Integer preparationMinutes,
    Money laborCostPerHour,
    Money cost
) {}

public record FixedCostLine(
    Money totalFixedCost,
    long dishesSold,
    Money perDishAllocation,
    String note    // e.g. "no orders in period — allocation set to zero"
) {}
```

---

## 4. The formula (pure helpers)

`core/cost/domain/util/DishCostFormula.java`:

```java
public final class DishCostFormula {

  /** Hard cap on prep minutes to prevent input abuse (24 h). */
  public static final int MAX_PREP_MINUTES = 24 * 60;

  private DishCostFormula() {}

  public static Money materialCost(List<MaterialLine> lines) {
    return lines.stream()
        .map(MaterialLine::lineCost)
        .reduce(Money.zero(Currency.COP), Money::plus);
  }

  public static Money laborCost(int prepMinutes, Money costPerHour) {
    if (prepMinutes < 0) throw new IllegalArgumentException("prepMinutes < 0");
    if (prepMinutes > MAX_PREP_MINUTES) throw new IllegalArgumentException("prepMinutes > 24h");
    if (costPerHour.isNegative()) throw new IllegalArgumentException("costPerHour < 0");

    // Hours is a quantity, not money — scale 6 + HALF_UP absorbs sub-minute drift
    // (matches existing CalculateProductCostService.SIXTY constant).
    BigDecimal hours = BigDecimal.valueOf(prepMinutes)
                              .divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);
    // Money.times(BigDecimal, RoundingMode) enforces final scale = currency default.
    return costPerHour.times(hours, RoundingMode.HALF_UP);
  }

  /**
   * Fixed cost per dish.
   * Edge case: if totalDishesSold == 0, returns Money.zero() with explanatory note.
   */
  public static FixedCostLine fixedCostAllocation(
      Money totalFixedCost, long totalDishesSold) {
    if (totalDishesSold <= 0) {
      return new FixedCostLine(totalFixedCost, 0,
          Money.zero(totalFixedCost.currency()),
          "no dishes sold in period — allocation set to zero");
    }
    Money perDish = totalFixedCost.divide(
        BigDecimal.valueOf(totalDishesSold),
        totalFixedCost.currency().getDefaultFractionDigits(),
        RoundingMode.HALF_UP);
    return new FixedCostLine(totalFixedCost, totalDishesSold, perDish, "");
  }

  /**
   * Total = material + labor + fixed. Each component has already been rounded to
   * currency-default scale by its respective helper; the sum keeps that scale.
   * No additional rounding pass here — re-rounding after a sum can flip a half-cent
   * and obscure the source of the discrepancy.
   */
  public static Money total(Money material, Money labor, Money fixedCost) {
    return material.plus(labor).plus(fixedCost);
  }
}
```

### 4.1 Numeric precision and order-of-operations

| Concern | Decision | Rationale |
|---|---|---|
| Rounding mode | `HALF_UP` | Matches existing `CalculateProductCostService.java:77`. Configurable via `app.money.rounding-mode`. |
| Intermediate scale (division) | 6 | Matches existing `CALC_SCALE = 6`. Absorbs sub-minute drift before final scale-2 round. |
| Final scale (storage, response) | `currency.getDefaultFractionDigits()` | COP/USD/EUR = 2; JPY = 0; KWD = 3. Currency-aware. |
| Order of summation | `material.plus(labor).plus(fixed)` — left-to-right | Each addend is already scale 2; sum is scale 2; no re-rounding pass. |
| `divide` always carries scale + mode | Compiler-enforced by `Money.divide(BigDecimal, int, RoundingMode)` | Prevents the classic `ArithmeticException: Non-terminating decimal expansion`. |
| Negative inputs | Rejected at the helper boundary | `IllegalArgumentException` → mapped to HTTP 400 by `GlobalExceptionHandler`. |
| `prepMinutes` clamp | `0..1440` (24 h) | Defends against accidental typos (e.g., 24000 → 400 hours of labor). |
| `dishesSold` floor | `>= 1` triggers allocation; `0` returns `Money.zero()` with note | Avoids division by zero (would throw `ArithmeticException` regardless). |

---

## 5. Application layer — `CalculateDishCostService`

```
DishCost calculate(CalculateDishCostCommand cmd)
```

```java
public record CalculateDishCostCommand(
    Long productId,
    YearMonth period,
    Currency currency    // optional override; defaults to system_configuration.default_currency
) {}
```

Flow:

1. **Load product** via `ProductRepositoryPort.findById(productId)` → 404 if absent.
2. **Load recipe** via `ProductRecipeQueryPort.findByProductId(productId)`. If empty → `ProductHasNoRecipeException` 422.
3. **Material cost**:
   - For each recipe line, fetch `SupplyVariant.unitCost` (already `Money` per Plan 06).
   - `lineCost = variant.unitCost × recipe.quantity`.
   - Aggregate.
4. **Labor cost**:
   - Read `system_configuration.cost_per_hour_of_cooking_labor`.
   - If `products.estimated_prep_minutes` is null → labor = `Money.zero()` (still allowed; document).
   - `labor = (minutes / 60) × costPerHour`.
   - If config missing → `MissingLaborCostConfigurationException` 422.
5. **Fixed cost allocation**:
   - Read `monthly_fixed_costs(period.year, period.month)` → `Money totalFixed`.
   - Read `orders` count where `status IN (:statusFilter)` and `date` ∈ `[period.firstDay, period.firstDay.plusMonths(1))` (boundaries computed in **business timezone** from `app.timezone` yml key, fallback to `system_configuration.business_timezone`) → `long dishesSold`.
     > **Time-zone correctness**: `orders.date` is stored as `LocalDateTime` (server-local; see `Order.java:25`). The bucket boundaries `[firstDay, firstDay.plusMonths(1))` MUST be converted with `ZoneId.of(businessTimezone)` so a 23:30 order in Bogota does not spill into the next month when the server runs in UTC. Implemented as a native query that binds the boundaries as `LocalDateTime` derived from the configured zone.
     > **Status filter**: `statusFilter` comes from `app.cost.dish-count.status-filter` (yml) with `Set<OrderStatus>` binding. Default = `[QUEUE, PREPARING, READY, DELIVERED]` (all non-cancelled). Counting non-cancelled is the conservative default — cost is incurred at order time, not at delivery. Narrow to `[DELIVERED]` only if business requires.
     > **`OrderStatus` enum**: defined in `core/order/domain/OrderStatus.java` — `QUEUE | PREPARING | READY | DELIVERED | CANCELLED`. The yml list binds to `Set<OrderStatus>`; values not matching the enum fail Spring Boot startup with a clear `ConfigurationProperties` error.
   - `allocation = DishCostFormula.fixedCostAllocation(totalFixed, dishesSold)`.
   - If `totalFixed` is missing for the period → `MissingFixedCostConfigurationException` 422.
6. **Total**: `material + labor + allocation` (all `Money.plus(...)`, never raw `BigDecimal.add(...)`).
7. Return `DishCost` with breakdown and `computedAt = Instant.now()`.

The computation runs **on-the-fly**; nothing is persisted. Persistence (snapshots) is Plan 05's job.

### 5.1 Repository ports (new)

```java
public interface SystemConfigurationRepositoryPort {
  Optional<String> findByKey(String key);
  String requireByKey(String key);             // throws MissingLaborCostConfigurationException
  void set(String key, String value, Long updatedBy);
}

public interface DishSalesCountRepositoryPort {
  long countDishesSoldIn(YearMonth period, Set<OrderStatus> statusFilter);  // uses business_timezone from config
}

public interface FixedCostQueryPort {
  Optional<FixedCost> findByPeriod(YearMonth period);
  FixedCost requireByPeriod(YearMonth period);  // throws MissingFixedCostConfigurationException
}
```

JPA adapters:
- `JpaSystemConfigurationAdapter` reads/writes `system_configuration` table.
- `JpaDishSalesCountAdapter` runs:
  ```sql
  SELECT COUNT(*)
  FROM   orders
  WHERE  `date` >= :start             -- bound as LocalDateTime in business TZ
    AND  `date` <  :endExclusive
    AND  status IN (:statusFilter);   -- bound as Set<OrderStatus>
  ```
  The `:start` / `:endExclusive` values are computed in the application using `ZoneId.of(businessTimezone)` so the bucket is correct regardless of the server clock's zone.
- `FixedCostAdapter` (in `infraestructure/fixedcosts/persistence/`) implements `FixedCostQueryPort`.

**Configuration binding** (`AppCostProperties.java`):

```java
@ConfigurationProperties(prefix = "app.cost")
@Validated
public record AppCostProperties(
    @NotNull BigDecimal laborCostPerHour,
    @NotNull Long defaultSupplyCategoryId,
    @NotNull DishCount dishCount,
    @NotNull MonthlyClosing monthlyClosing
) {
  public record DishCount(@NotEmpty Set<OrderStatus> statusFilter) {}
  public record MonthlyClosing(
      boolean enabled,
      @NotBlank String cron,
      @NotBlank String zone,
      @Positive int batchSize,
      @NotNull Duration lockAtMostFor,
      @NotNull Duration lockAtLeastFor
  ) {}
}
```

Spring Boot binds `app.cost.dish-count.status-filter` (yml list of strings) to `Set<OrderStatus>` automatically via `ApplicationConversionService`. A typo in yml (e.g., `DELIVERD`) fails startup with a clear message — preferable to silently counting zero orders.

All read paths in this use case are `@Transactional(readOnly = true)`; the underlying query ports do not acquire locks (the cost is a snapshot of mutable inputs at read time — the closing job in Plan 05 is what freezes it).

---

## 6. Alternative labor cost: per-area salary

The user prompt mentions "salary of workers / 160h × estimated prep time". Today's `users` already carry `salary_history`. Two strategies:

### 6.1 Default: global hourly rate (simpler)

```
labor = (prepMinutes / 60) × system_configuration.cost_per_hour_of_cooking_labor
```

This is the **v1** formula. Implemented here.

### 6.2 Optional: per-area average (richer)

```
avgHourlyRate = AVG(active user salaries in product.area) / 160   // 160h/month
labor = (prepMinutes / 60) × avgHourlyRate
```

This aligns with the in-progress `CalculateProductCostUseCase` (activity in `activities.json`). When that activity lands, **prefer 6.2 when `product.area_id` is set**, falling back to 6.1 otherwise. The plan keeps both behind the same `LaborLine` shape so the caller cannot tell the difference.

---

## 7. REST API surface

Base path: `/api/v1/products` (extends existing `ProductController`). `@Tag(name = "Products")`.

| Method | Path | Purpose | Status |
|---|---|---|---|
| `GET` | `/products/{id}/cost` | On-the-fly cost (current month default) | 200 / 404 / 422 |
| `GET` | `/products/{id}/cost?year=2026&month=7` | On-the-fly cost for period | 200 / 404 / 422 |
| `GET` | `/products/{id}/cost/history` | List of historical snapshots (Plan 05) | 200 |
| `GET` | `/system/configuration/{key}` | Read a setting | 200 / 404 |
| `PUT` | `/system/configuration/{key}` | Update setting (admin only) | 200 / 400 |

### Response

```json
// GET /products/42/cost?year=2026&month=7
{
  "productId": 42,
  "productName": "Hamburguesa Especial",
  "period": { "year": 2026, "month": 7 },
  "materialCost":   { "amount": 5.40, "currency": "COP" },
  "laborCost":      { "amount": 2.40, "currency": "COP" },
  "fixedCostAllocation": { "amount": 1.10, "currency": "COP" },
  "totalCost":      { "amount": 8.90, "currency": "COP" },
  "breakdown": {
    "materials": [
      { "supplyVariantId": 12, "supplyName": "Carne 250g", "quantity": 0.250, "unitCost": { "amount": 18.00, "currency": "COP" }, "lineCost": { "amount": 4.50, "currency": "COP" } },
      { "supplyVariantId": 31, "supplyName": "Pan",        "quantity": 1.000, "unitCost": { "amount": 0.90, "currency": "COP" }, "lineCost": { "amount": 0.90, "currency": "COP" } }
    ],
    "labor": {
      "preparationMinutes": 12,
      "laborCostPerHour":   { "amount": 12000.00, "currency": "COP" },
      "cost":               { "amount": 2400.00, "currency": "COP" }
    },
    "fixedCost": {
      "totalFixedCost": { "amount": 9050000.00, "currency": "COP" },
      "dishesSold": 8231,
      "perDishAllocation": { "amount": 1099.62, "currency": "COP" },
      "note": ""
    }
  },
  "computedAt": "2026-07-14T10:05:00Z"
}
```

(All money is `Money` per Plan 06; the wire format `{ amount, currency }` applies.)

---

## 8. Concurrency

- The cost is computed **on the fly** — no shared mutable state.
- The underlying queries (`supply_variants.unit_cost`, `monthly_fixed_costs`, `orders count`) take whatever locks they need (see Plans 01, 02, 03).
- `CalculateDishCostUseCase` itself is `@Transactional(readOnly = true)` — read-only snapshot.

---

## 9. Edge cases

| Case | Handling |
|---|---|
| Product has no recipe | `ProductHasNoRecipeException` 422 with hint "Add a recipe first" |
| `estimated_prep_minutes` null | Labor cost = `Money.zero()`; document the trade-off (encourage admin to fill) |
| `cost_per_hour_of_cooking_labor` missing | `MissingLaborCostConfigurationException` 422 |
| Fixed costs missing for the period | `MissingFixedCostConfigurationException` 422 |
| `dishesSold == 0` | Allocation = `Money.zero()` with `note = "no dishes sold in period — allocation set to zero"` (no 500) |
| Currency mismatch (FE asks in USD, system is COP) | Convert or 422; v1 rejects mismatched currency → 422 "currency conversion not supported" |
| Order placed at 23:30 Bogota on the last day of the month, server runs in UTC | Bucket boundaries computed in business TZ; the order lands in the correct month. Verified by a TZ-shift IT (server `UTC`, business `America/Bogota`). |
| Very large product with 100+ recipe lines | No issue; pure in-memory aggregation |
| Recursion: recipe references a `ProductOption` recipe | Out of scope here; `SpecialSelectionPricingService` already aggregates option recipes — merge at API level (see § 10) |
| Concurrent `CalculateDishCost` calls | Each call is `@Transactional(readOnly = true)` and stateless; no shared mutable state. |

---

## 10. Interaction with `SpecialSelectionPricingService`

`SpecialSelectionPricingService` already computes a "raw material cost" for price suggestion (margin tool). To avoid duplicated math, **refactor** it to call `DishCostFormula.materialCost(...)` after `CalculateDishCostService.materialCostFor(productId)` becomes available. Both code paths then share one tested implementation.

---

## 11. Tests

| Test class | Coverage |
|---|---|
| `DishCostFormulaTest` | Pure math — material sum, labor (incl. prep = 0, prep = 1440, prep = -1, prep = 99999), fixed allocation, dishesSold = 0 |
| `DishCostFormulaPropertyTest` (jqwik) | Property-based: for any `(S, P, Q, P')`, computed `P_new` is between `min(P, P')` and `max(P, P')`. Allocations sum to original. |
| `CalculateDishCostServiceTest` | Stub all ports; assert breakdown + total |
| `CalculateDishCostServiceTest` | Missing recipe → 422 |
| `CalculateDishCostServiceTest` | Missing labor config → 422 |
| `CalculateDishCostServiceTest` | Missing fixed cost config → 422 |
| `CalculateDishCostServiceTest` | DishesSold = 0 → allocation zero + note |
| `CalculateDishCostServiceTest` | Invalid status filter (empty set) → 422 |
| `CalculateDishCostTimezoneIT` | Server in UTC, business TZ America/Bogota, order at 23:30 Bogota last day of month → counted in correct month |
| `DishCostControllerIT` | Swagger + 200/404/422 mapping + RFC 7807 Problem Details body shape |
| `SystemConfigurationAdapterIT` | Read/write round-trip |
| `AppCostPropertiesBindingTest` | yml with invalid status (e.g., `DELIVERD`) fails startup with a clear message |

---

## 12. Acceptance criteria

- [ ] `V33` creates `system_configuration` and seeds defaults; `V34` adds `estimated_prep_minutes`.
- [ ] `CalculateDishCostService` returns `DishCost` with full breakdown.
- [ ] Formula matches the prompt: `Materials + Labor + FixedCost/totalDishes`.
- [ ] `dishesSold == 0` handled without 500.
- [ ] `GET /products/{id}/cost?year=&month=` returns proper response.
- [ ] `Money` value object (Plan 06) used everywhere — no raw `BigDecimal` in `core/cost/domain/**`.
- [ ] `./harness/harness.sh` exits 0.

---

## 13. Risks / follow-ups

- **Counting "sold"**: choice between (a) all non-cancelled orders, (b) only `DELIVERED`, (c) only `READY`. Document chosen policy and make it configurable via `system_configuration.dish_count_status_filter` if business disagrees.
- **Labor formula**: today uses global hourly rate. If per-area salary becomes the default, swap § 6.2 in.
- **Currency**: single currency today (COP). Add FX rate table when needed.
- **Time zone**: `business_timezone` config governs the `period` boundary; Plan 05's cron uses the same value to avoid off-by-one month rolls.