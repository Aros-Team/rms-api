# Plan 06 — Money Value Object Refactor (Centralize BigDecimal Money Handling)

> Cross-cutting refactor. Bounded context: `common/money` (new). Wraps every monetary `BigDecimal` in a typed `Money` value object so arithmetic, rounding, currency, and scale are enforced in **one place** — the domain.

---

## 1. Why this refactor

The codebase currently uses **raw `BigDecimal`** for every monetary column (`unit_cost`, `base_price`, `unit_price`, `total_amount`, `line_total`, `salary`, etc.). This causes recurring problems:

| Problem | Concrete example in current code |
|---|---|
| Inconsistent scale | `unit_cost DECIMAL(10,2)` vs `total_amount DECIMAL(12,2)` vs `line_total` re-multiplied elsewhere |
| Scattered rounding | Each service calls `.setScale(2, RoundingMode.HALF_UP)` ad-hoc — easy to forget |
| Mixed currencies silently | No way to assert "this `BigDecimal` is in COP, that one is in USD" |
| Multiplication ambiguity | `unit_price × quantity` uses raw `BigDecimal.multiply`; nothing prevents `.multiply(String)` or accidental `double` casts |
| No percentage primitive | Tax / margin / discount math is reinvented per use case |
| Pervasive nullability | `null` and `BigDecimal.ZERO` are both used to mean "no money"; invariants are weak |

**Goal**: a single, framework-free `Money` value object that becomes the canonical type for money in **domain** and **application** layers. Persistence (`BigDecimal` columns) is handled by the mapper layer; the domain never sees raw `BigDecimal` for money.

### 1.1 Why not JSR 354 (`javax.money.MonetaryAmount`)?

JSR 354 is the JCP standard for money handling in Java. We **considered** it and chose a custom `Money` for these reasons:

| Concern | JSR 354 (`org.javamoney.moneta`) | Custom `Money` (this plan) |
|---|---|---|
| Domain purity (no framework imports) | Adds `javax.money:moneta` + an SPI to the domain classpath | Zero dependencies — pure Java records |
| Per-currency rounding | First-class (`Monetary.getRounding(CurrencyUnit)`) | One-line helper; same effect |
| ISO 4217 currency list | Built-in (`CurrencyUnit.getCurrencyCode()`) | Reuse `java.util.Currency` (also ISO 4217) |
| Allocation / splits | `MonetaryAmount.split(divide, var…)` exists | Custom but 20 lines |
| Multi-currency conversion | First-class (`CurrencyConversion`) | Deferred — single-currency for now |
| Learning curve | New API surface | Engineers already know `BigDecimal` |

**Decision**: custom `Money` for v1. If multi-currency or external FX ever lands, we can replace `Money` with `org.javamoney.moneta.Money` *behind the same record shape* — adapters stay the same, callers do not break.

### 1.2 Spring Boot 4 / Jakarta EE 11 reminder

- All persistence imports use `jakarta.persistence.*` (not `javax.persistence.*`).
- All web annotations are still `org.springframework.web.bind.annotation.*` (unchanged).
- Hibernate moved to its own module in Boot 4 (`org.springframework.boot.hibernate.autoconfigure.*`); `@EnableTransactionManagement` is auto-configured when `spring-boot-starter-data-jpa` is on the classpath — no explicit annotation needed.

---

## 3. Architectural placement

Hexagonal layering (per `docs/architecture.md`):

```
core/common/money/
├── domain/
│   ├── Money.java              // immutable value object (record)
│   ├── RoundingMode.java       // re-export of java.math.RoundingMode for domain purity
│   ├── MoneyContext.java       // optional thread-local rounding / scale context
│   └── exception/
│       ├── CurrencyMismatchException.java
│       ├── NegativeMoneyException.java
│       ├── DivisionByZeroMoneyException.java
│       └── InvalidMoneyScaleException.java
└── application/
    └── MoneyCalculator.java    // pure helpers: percentage, allocation, weighted-average, tax-inclusive split
```

**Why no custom `Currency` enum**: we reuse `java.util.Currency`, which already carries the ISO 4217 code, symbol, default fraction digits, and is part of the JDK. `Money.currency()` returns `Currency` (the JDK type), not a project-local enum.

```java
public record Money(BigDecimal amount, Currency currency) {
  // Currency = java.util.Currency (java.util, not java.math)
}
```

**Layer rules** (enforced by harness §6):

| Layer | Allowed | Forbidden |
|---|---|---|
| `domain/common/money/` | `java.math.BigDecimal`, `java.math.RoundingMode`, `java.util.Currency`, Lombok `@Value` | `org.springframework.*`, `jakarta.persistence.*` |
| `application/common/money/` | Domain types, ports, plain Java | Framework annotations |
| `infrastructure/common/money/` | Spring config (currency provider), MapStruct mappers | Reaching into domain for new abstractions |

> **Harness enforcement**: add a new check in `harness/harness.sh` that greps `src/main/java/aros/services/rms/core/**/domain/**/*.java` for `import java.math.BigDecimal;` and fails the build if any match. Allowlist: only files under `domain/common/money/` itself.

---

## 4. `Money` value object — contract

```java
public record Money(BigDecimal amount, Currency currency) {

  public static final int DEFAULT_SCALE = 2;

  public Money {
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(currency, "currency");
    if (amount.scale() > 10) {
      throw new InvalidMoneyScaleException(amount.scale());
    }
    // Normalize to the currency's default fraction digits (e.g., COP = 2, JPY = 0)
    amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
  }

  // ----- factories -----
  public static Money zero(Currency ccy) { return new Money(BigDecimal.ZERO, ccy); }
  public static Money of(String s, Currency ccy) { return new Money(new BigDecimal(s), ccy); }
  public static Money of(long v, Currency ccy)   { return new Money(BigDecimal.valueOf(v), ccy); }
  // NOTE: no `of(double, ...)` — doubles silently lose precision; reject at compile time.

  // ----- accessors -----
  public BigDecimal amount() { return amount; }                     // for persistence only
  public boolean isPositive() { return amount.signum() > 0; }
  public boolean isZero()     { return amount.signum() == 0; }
  public boolean isNegative() { return amount.signum() < 0; }

  // ----- arithmetic (currency-safe) -----
  public Money plus(Money other) {
    assertSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }
  public Money minus(Money other) {
    assertSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }
  public Money times(BigDecimal multiplier) {
    Objects.requireNonNull(multiplier, "multiplier");
    return new Money(amount.multiply(multiplier), currency);
  }
  public Money times(BigDecimal multiplier, RoundingMode mode) {
    Objects.requireNonNull(mode, "mode");
    BigDecimal scaled = amount.multiply(multiplier)
                              .setScale(currency.getDefaultFractionDigits(), mode);
    return new Money(scaled, currency);
  }
  public Money divide(BigDecimal divisor, int scale, RoundingMode mode) {
    Objects.requireNonNull(divisor, "divisor");
    if (divisor.signum() == 0) throw new DivisionByZeroMoneyException(currency);
    return new Money(amount.divide(divisor, scale, mode), currency);
  }
  public Money negated() { return new Money(amount.negate(), currency); }
  public Money abs()     { return new Money(amount.abs(), currency); }

  // ----- percentage -----
  public Money percent(BigDecimal pct) {
    Objects.requireNonNull(pct, "pct");
    BigDecimal fraction = pct.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
    return new Money(amount.multiply(fraction)
                           .setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP),
                     currency);
  }
  public Money applyMargin(BigDecimal marginPct) { return plus(percent(marginPct)); }

  // ----- allocation (e.g., split tax across N line items without losing cents) -----
  public List<Money> allocate(int parts) { ... }                    // largest-remainder method
  public List<Money> allocate(List<BigDecimal> ratios) { ... }      // weighted split

  // ----- comparison -----
  public boolean isGreaterThan(Money other)  { assertSameCurrency(other); return amount.compareTo(other.amount) > 0; }
  public boolean isGreaterOrEqual(Money o)   { assertSameCurrency(o); return amount.compareTo(o.amount) >= 0; }
  public boolean isLessThan(Money other)     { assertSameCurrency(other); return amount.compareTo(other.amount) < 0; }
  public int compareTo(Money other)          { assertSameCurrency(other); return amount.compareTo(other.amount); }

  // ----- helpers -----
  private void assertSameCurrency(Money other) {
    if (other == null || !currency.equals(other.currency)) {
      throw new CurrencyMismatchException(currency, other == null ? null : other.currency);
    }
  }

  @Override public String toString() { return currency.getCurrencyCode() + " " + amount.toPlainString(); }
}
```

**Why `Currency` is `java.util.Currency`**:
- Already ISO 4217 compliant — 180+ currencies with their native fraction digits.
- `getDefaultFractionDigits()` gives us 2 for COP/USD/EUR, 0 for JPY, 3 for KWD, etc. — no manual table.
- Thread-safe, immutable, JDK-blessed.

Currency resolution at the entry point (controllers, scheduled jobs):

```java
Currency ccy = Currency.getInstance(
    systemConfigurationPort.requireByKey("default_currency"));  // "COP"
// All Money values in this request are Money.of(amount, ccy).
```

---

## 5. Existing monetary fields — migration map

The following fields are migrated **domain-side only**; columns stay `BigDecimal`.

| Bounded context | Domain field (current) | New type | Adapter |
|---|---|---|---|
| `inventory` | `SupplyVariant.unitCost` | `Money` | `SupplyVariantMapper` (existing, updated) |
| `inventory` | `InventoryMovement` — none | — | — |
| `product` | `Product.basePrice` | `Money` | `ProductMapper` (existing, updated) |
| `purchase` | `PurchaseOrder.totalAmount` | `Money` | `PurchaseOrderMapper` (existing, updated) |
| `purchase` | `PurchaseOrderItem.unitPrice` | `Money` | `PurchaseOrderItemMapper` (existing, updated) |
| `purchase` | `PurchaseSessionItem.lineTotal` | `Money` | new mapper |
| `purchase` | `PurchasePreviewLine.before/after.unitCost` | `Money` | new mapper |
| `order` | `OrderDetail.unitPrice` | `Money` | `OrderDetailMapper` (existing, updated) |
| `user` | `Salary.amount` | `Money` | new mapper |
| `user` | `SalaryHistoryEntry.old/newSalary.amount` | `Money` | new mapper |
| `fixedcosts` | all fields (Plan 03) | `Money` | new mapper |
| `cost` | `DishCost.total/material/labor/fixedCost` | `Money` | new mapper |

### 5.1 Adapter pattern

Every entity keeps `BigDecimal` (DB constraint). The mapper is the **only** place where conversion happens:

```java
public class SupplyVariantMapper {
  public SupplyVariant toDomain(SupplyVariantEntity e) {
    if (e == null) return null;
    return new SupplyVariant(
        e.getId(),
        e.getSupplyId(),
        e.getUnitId(),
        e.getQuantity(),
        Money.of(e.getUnitCost(), Currency.COP));   // ← conversion
  }

  public SupplyVariantEntity toEntity(SupplyVariant d) {
    if (d == null) return null;
    return SupplyVariantEntity.builder()
        ...
        .unitCost(d.getUnitCost().amount())          // ← back to BigDecimal
        .build();
  }
}
```

For DTOs returned to the FE, the response object carries `Money` and Jackson serializes it via a small `@JsonComponent`:

```java
@JsonComponent
public class MoneyJsonSerializer extends StdSerializer<Money> { ... }   // emits { amount, currency }
```

---

## 6. Use cases that must migrate first

These perform arithmetic and are the source of rounding bugs. Migrate them in this order; each task gets its own activity in `activities.json`:

1. **`WeightedAverageCalculator`** (Plan 01) — replaces raw `BigDecimal` math with `Money`.
2. **`RegisterPurchaseOrderService`** (existing) — wraps all `unit_price`, `total_amount` in `Money`.
3. **`SpecialSelectionPricingService`** (existing) — already does `unitCost × qty`; migrate.
4. **`CalculateProductCostUseCase`** (Plan 04, in progress) — adopt `Money` from day one.
5. **`FixedCostsService`** (Plan 03) — uses `Money` throughout.
6. **`MonthlyClosingJob`** (Plan 05) — `totalFixed / totalDishes` is `Money.divide(BigDecimal, scale, mode)`.
7. **`SalaryHistoryService`** (existing) — wraps `old/newSalary` in `Money`.

---

## 7. Standard rounding policy

### 7.0 Why `HALF_UP` (and not `HALF_EVEN`)

Financial software canon often uses **banker's rounding** (`HALF_EVEN`) because it removes the systematic upward bias of repeated `HALF_UP` rounding. We picked `HALF_UP` for one reason: **the existing `CalculateProductCostService` already uses `HALF_UP`** (see `core/product/application/service/CalculateProductCostService.java:77`), and the existing DB columns match scale 2 + HALF_UP semantics. Consistency with the deployed code wins over textbook purity.

If the project later needs `HALF_EVEN` (e.g., for IFRS reporting), it is a one-line config change:

```yaml
app:
  money:
    rounding-mode: HALF_EVEN   # was HALF_UP
```

`Money` reads this once at startup via `RoundingMode.valueOf(props.getRoundingMode())` and threads it through `MoneyCalculator`. Per-call override remains available (e.g., weighted-average intermediate math can force `HALF_UP` to match historical columns even if display rounds `HALF_EVEN`).

### 7.0.1 RFC 7807 Problem Details for error responses

All `Money`-related exceptions (and every other exception in the project) MUST be translated by `GlobalExceptionHandler` to Spring Boot 4's `ProblemDetail` (`org.springframework.http.ProblemDetail`). This is the IETF standard (RFC 7807 / RFC 9457) for HTTP error bodies.

```java
@ExceptionHandler(CurrencyMismatchException.class)
public ProblemDetail handleCurrencyMismatch(CurrencyMismatchException ex) {
  ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
  pd.setType(URI.create("https://rms.spalaxd.co/errors/currency-mismatch"));
  pd.setTitle("Currency mismatch");
  pd.setProperty("expectedCurrency", ex.getExpectedCurrency().getCurrencyCode());
  pd.setProperty("actualCurrency",   ex.getActualCurrency().getCurrencyCode());
  return pd;
}
```

Replace ad-hoc `Map<String, String>` error bodies throughout the codebase with `ProblemDetail`.

| Operation | Scale | Mode | Why |
|---|---|---|---|
| Display / persistence | `Currency.getDefaultFractionDigits()` (2 for COP/USD/EUR) | `HALF_UP` (configurable) | Matches existing DB columns and `CalculateProductCostService` |
| Weighted average intermediate | 4 | `HALF_UP` (hardcoded) | Reduce drift before final round to 2; do not let config affect historical reconciliation |
| Percentage application | currency default | `HALF_UP` (configurable) | Predictable for humans |
| Allocation splits | currency default | `HALF_UP` (configurable) | Largest-remainder keeps `sum(parts) == total` |
| Tax / discount | currency default | `HALF_UP` (configurable) | TBD with tax engine plan |

`MoneyCalculator` exposes helpers so the policy lives in **one** file:

```java
public final class MoneyCalculator {
  public static Money weightedAverage(Money current, BigDecimal currentQty,
                                      Money purchased, BigDecimal purchasedQty) { ... }
  public static List<Money> splitEvenly(Money total, int parts) { ... }
  public static Money applyPercentage(Money base, BigDecimal pct) { ... }
  // ...
}
```

### 7.1 The `divide` rule (common bug source)

`BigDecimal` throws `ArithmeticException` on inexact division with no rounding mode. **Every** `divide` call in the codebase MUST specify scale and rounding mode. The `Money.divide(...)` signature above makes this impossible to forget — the compiler enforces it. Any raw `BigDecimal.divide(other)` in the codebase is a bug; the harness rule (§ 3) flags it.

```java
// BAD — ArithmeticException at runtime when result is non-terminating
BigDecimal perDish = totalFixed.divide(BigDecimal.valueOf(dishesSold));

// GOOD — Money forces scale + mode
Money perDish = totalFixed.divide(
    BigDecimal.valueOf(dishesSold),
    totalFixed.currency().getDefaultFractionDigits(),
    RoundingMode.HALF_UP);
```

### 7.2 Intermediate-scale discipline (existing project pattern)

The existing `CalculateProductCostService` uses `CALC_SCALE = 6` for intermediate division before the final `setScale(COST_SCALE=2, HALF_UP)` for storage. We adopt the same pattern in `MoneyCalculator`:

```java
public static Money laborCost(int prepMinutes, Money costPerHour) {
  // Hours is a quantity, not money — scale 6 + HALF_UP absorbs sub-minute drift.
  // This matches the existing CalculateProductCostService.SIXTY constant.
  BigDecimal hours = BigDecimal.valueOf(prepMinutes)
                            .divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);
  return costPerHour.times(hours, RoundingMode.HALF_UP);
}
```

Rationale: scale 2 on intermediate division loses 4 digits of precision per operation; scale 6 keeps a 4-decimal safety margin. The existing project chose 6 — keep it.

---

## 8. SQL / DB impact

**None.** All DB columns keep their existing `DECIMAL(p, s)` definitions. Mappers translate in/out. This keeps the refactor **non-breaking** at the DB level.

A future migration (not part of this plan) may add a `currency CHAR(3) NOT NULL DEFAULT 'COP'` column to all money tables if multi-currency support is approved. For now, currency is **inferred** from `system_configuration.default_currency` (Plan 04).

---

## 9. REST API impact

Response DTOs gain a typed representation. Old clients keep working because Jackson emits `amount` + `currency`:

```json
// before
{ "unitCost": 1.20 }

// after
{ "unitCost": { "amount": 1.20, "currency": "COP" } }
```

> **Breaking change flag.** Coordinate with frontend team. Two options:
>
> - **Soft rollout** (recommended): response includes both fields for one release.
>   ```json
>   { "unitCost": 1.20, "unitCostMoney": { "amount": 1.20, "currency": "COP" } }
>   ```
> - **Hard cutover**: announce a date, ship together with FE changes.

---

## 10. Tests

| Test class | Coverage |
|---|---|
| `MoneyTest` | Constructors, arithmetic, equality, currency mismatch, negative rejected |
| `MoneyCalculatorTest` | `weightedAverage`, `splitEvenly`, `applyPercentage` |
| `SupplyVariantMapperTest` | `BigDecimal ↔ Money` round-trip, scale normalization |
| `MoneyJsonSerializerTest` | Jackson output shape |
| `ArchitectureTest` (ArchUnit) | No `import java.math.BigDecimal` in `domain/` outside `common/money/` |
| `MoneyMigrationIT` | DB column unchanged; entity persistence round-trip |

---

## 11. Acceptance criteria

- [ ] `Money`, `Currency`, `MoneyCalculator` exist under `core/common/money/` with full unit test coverage.
- [ ] All monetary domain fields listed in § 5 are typed as `Money`.
- [ ] All mappers listed in § 5 convert `BigDecimal ↔ Money` losslessly.
- [ ] Jackson serialization shape agreed with frontend (soft or hard).
- [ ] Harness grep rule added and green.
- [ ] No regression in `./harness/harness.sh`.

---

## 12. Rollout plan

| Step | Description | Risk |
|---|---|---|
| 1 | Add `Money` + tests, no callers | none |
| 2 | Migrate `WeightedAverageCalculator` (Plan 01) | low — isolated |
| 3 | Migrate `SupplyVariant` + mapper | low |
| 4 | Migrate `Product.basePrice` + mapper | low |
| 5 | Migrate `PurchaseOrder/Item` + mappers | medium — touches confirm flow |
| 6 | Migrate `OrderDetail.unitPrice` + mapper | medium — affects order endpoints |
| 7 | Migrate `Salary` + `SalaryHistoryEntry` | low |
| 8 | Adopt in new code: `FixedCosts`, `MonthlyClosing`, `CalculateProductCost` | low — greenfield |
| 9 | Remove legacy `BigDecimal` from domain; enforce harness rule | medium — touches many files |

Each step ships independently behind `feat/money-*` branches; the harness stays green at every step.

---

## 13. Risks / open questions

- **Soft rollout vs hard cutover**: needs FE alignment (see § 9).
- **Multi-currency**: not in scope; revisit when needed. Single `default_currency` from `system_configuration` is the working assumption; resolve to `java.util.Currency` at the boundary.
- **Thread-local rounding context**: considered but rejected for v1 — keep `Money` self-describing.
- **Performance**: `Money` allocates one `BigDecimal` per operation; negligible compared to DB I/O. Benchmark if a hot path emerges.
- **Migration fatigue**: 7 bounded contexts touched. Use the orchestrator pattern — one activity per context, one task per file group.
- **JSR 354 future**: if we ever adopt `org.javamoney.moneta`, wrap it in the same `Money` record shape so callers do not break. `Money.of(BigDecimal, java.util.Currency)` becomes `Money.of(MonetaryAmount)` internally; all the other Money methods stay.