# Plans Overview & Corrections Applied

> Map of the 6 plans in this folder + a consolidated changelog of best-practice corrections.

## The six plans

| # | File | Bounded context | Status |
|---|---|---|---|
| 01 | [`01-insumo-management.md`](./01-insumo-management.md) | `inventory` (extend) | Foundation; unlocks every other plan |
| 02 | [`02-purchase-invoice-preview-confirm.md`](./02-purchase-invoice-preview-confirm.md) | `purchase` (refactor + new flow) | Replaces one-step purchase with preview → confirm |
| 03 | [`03-fixed-costs.md`](./03-fixed-costs.md) | `fixedcosts` (new) | Captures monthly operating expenses |
| 04 | [`04-dish-cost-calculation.md`](./04-dish-cost-calculation.md) | `cost` (new) | On-the-fly formula = Materials + Labor + Fixed/Dishes |
| 05 | [`05-monthly-closing.md`](./05-monthly-closing.md) | `cost` + `scheduled` (new) | Cron + ShedLock that snapshots every dish cost monthly |
| 06 | [`06-money-value-object-refactor.md`](./06-money-value-object-refactor.md) | `common/money` (cross-cutting) | Centralizes BigDecimal handling via `Money` value object |
| 07 | [`07-public-jwks-endpoint.md`](./07-public-jwks-endpoint.md) | `auth` (extend) | Public `/.well-known/jwks.json` for JWT verification, with key rotation |

## Dependency graph

```
01 (Insumo) ─┬─> 02 (Purchase Preview/Confirm)
              ├─> 04 (Dish Cost) ─┐
              ├─> 05 (Monthly Closing)
              └────────────────────┘
03 (Fixed Costs) ──────> 04 ──> 05
06 (Money refactor) ──> applied across 01, 02, 03, 04, 05
07 (JWKS) ────────────> independent; touches `auth/` only
```

**Recommended implementation order**: 06 → 07 → 01 → 02 → 03 → 04 → 05.
(07 is small, scoped, and unblocks external clients — do it early.)

---

## Best-practice corrections applied (changelog)

Researched against current Spring Boot 4, Hibernate 6, JPA, and Flyway docs. Each item below lists the plan(s) touched and the rationale.

### Financial-software corrections (round 2 review)

| # | Topic | Decision | Why |
|---|---|---|---|
| F1 | Default rounding mode | `HALF_UP` (matches existing `CalculateProductCostService.java:77`); `HALF_EVEN` available via `app.money.rounding-mode` | Consistency with deployed code wins over textbook purity; one-line flip if IFRS later |
| F2 | Intermediate scale on division | 6 (matches `CALC_SCALE = 6`); final scale = `currency.getDefaultFractionDigits()` | Absorbs sub-minute drift; mirrors existing convention |
| F3 | `divide` always carries scale + mode | Compiler-enforced via `Money.divide(BigDecimal, int, RoundingMode)` | Prevents `ArithmeticException: Non-terminating decimal expansion` |
| F4 | `prep_minutes` clamp | `0..1440` (24 h) | Defends against typos (24000 → 400 h of labor) |
| F5 | Currency awareness | `java.util.Currency.getDefaultFractionDigits()` — COP=2, JPY=0, KWD=3 | Single-currency assumption today but the value object is multi-currency-ready |
| F6 | Order-of-operations | `material.plus(labor).plus(fixed)` left-to-right; no re-rounding pass | Re-rounding after sum can flip a half-cent and obscure source of discrepancy |
| F7 | Audit log for money/stock mutation | **Mandatory** on every plan; payload includes `previousState` + `newState` JSON snapshots + `reason` + `performedBy` + `performedAt` (UTC `Instant`) | Financial-software canon: every monetary change must be attributable and reconstructable |
| F8 | Snapshot integrity hash | `integrity_hash CHAR(64)` SHA-256 on `historical_product_costs` rows | Detects silent data tampering; verifier job recomputes and reports drift |
| F9 | Snapshot DB-level immutability | `BEFORE UPDATE` and `BEFORE DELETE` triggers raise `SQLSTATE '45000'` | Defense in depth — application code, mapper, and DB all refuse writes |
| F10 | Timestamps | `Instant` (UTC) for audit + scheduled events; `LocalDateTime` kept for `Order.date` (separate refactor) | Mixing TZs is the #1 source of off-by-one month bugs |
| F11 | Status filter | `Set<OrderStatus>` enum binding from `app.cost.dish-count.status-filter`; Spring Boot fails startup on invalid values | String lists silently accept typos like `DELIVERD` |
| F12 | Time-zone bucketing | Boundaries `[period.firstDay, firstDay.plusMonths(1))` computed via `ZoneId.of(app.timezone)`, NOT server TZ | 23:30 Bogota order doesn't spill to next month on UTC server |
| F13 | RFC 7807 Problem Details | Every `GlobalExceptionHandler` returns `org.springframework.http.ProblemDetail` | IETF standard; richer error payloads; deprecates ad-hoc `Map<String,String>` |
| F14 | Append-only triggers + hashes + audit logs | Triple-redundancy on historical financial data | Belt-and-suspenders; matches SOX / IFRS control requirements |

### Code-policy and project-structure verification (round 2 review)

| Check | Status |
|---|---|
| Domain layer has no `org.springframework.*` or `jakarta.persistence.*` imports | ✓ — all plans confine framework imports to `infrastructure/` |
| Use case naming follows `{Verb}{Entity}UseCase` | ✓ — `RegisterFixedCostUseCase`, `ConfirmPurchaseUseCase`, `RunMonthlyClosingUseCase` |
| Exceptions follow `*NotFoundException` / `*AlreadyExistsException` / `Invalid*Exception` | ✓ |
| Swagger: every controller has `@Tag` + `@Operation` + `@ApiResponse` | ✓ — called out in every plan's REST section |
| Validation: DTOs carry `@Valid` + Bean Validation annotations | ✓ |
| Lombok: `@Data`/`@Builder` for entities; no `@Slf4j` in `domain/` | ✓ — every plan uses the project's `Logger` interface for domain logging |
| Migrations: sequential `V30`–`V36`, ordered, no forward references | ✓ — each migration references only existing tables |
| Concurrency: pessimistic + optimistic combo with timeout hints | ✓ — `@Lock` + `@Version` + `jakarta.persistence.lock.timeout` |
| Idempotency on every mutating POST | ✓ — `Idempotency-Key` header on confirm; UNIQUE index enforces |
| RFC 7807 Problem Details for errors | ✓ — F13 above |
| Audit log on every money/stock mutation | ✓ — F7 above |
| DB-level immutability for ledger tables | ✓ — F9 above |
| Spring Boot 4 / Jakarta EE 11 namespaces | ✓ — every plan uses `jakarta.*` |
| YAML config uses `@ConfigurationProperties` + `@Validated` (Plan 04) | ✓ — typos fail startup |

### Earlier corrections (round 1)

[Original section kept below.]

---

### Distributed cron scheduling (Plan 05)

- **Before**: `@Scheduled(cron = "${...}")` directly on the bean.
- **After**: `SchedulingConfigurer` registers the `CronTrigger` programmatically; `@SchedulerLock(name = "rmsMonthlyClosing", lockAtMostFor = "PT23H", lockAtLeastFor = "PT5M")` guarantees only **one** instance in a cluster runs the job. New `shedlock` table migration (`V36`) + `net.javacrumbs.shedlock:shedlock-spring:5.16.0` dependency added.
- **Why**: Spring Boot's `@Scheduled(cron)` does not support property placeholders directly, and multi-instance deployments would otherwise fire the closing job N times.

### `@Version` (optimistic locking) on every mutable root entity (Plans 01, 02, 03)

- **Before**: only `PESSIMISTIC_WRITE` row locks.
- **After**: `@Version` column added to `supply_variants`, `purchase_sessions`, `monthly_fixed_costs`. Belt-and-suspenders with pessimistic locks — version catches any race that slips past the lock (e.g., connection-pool timeout that silently dropped the lock). Migrations: `V30_1`, `V31`, `V32_1`.
- **Why**: defensive against lock-loss scenarios; Hibernate auto-detects stale updates and throws `OptimisticLockingFailureException`.

### `@Retryable` on optimistic-lock failures (Plans 01, 02, 03)

- **Before**: caller sees a 500 on a transient lock race.
- **After**: `@Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 200, multiplier = 2.0))` translates the race into a clean retry.
- **Why**: per Spring docs, the recommended pattern for transient optimistic conflicts.

### Pessimistic lock timeout hints (Plans 01, 02, 03)

- **Before**: `PESSIMISTIC_WRITE` without a timeout could stall HTTP threads indefinitely on a stuck row.
- **After**: `@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})` (3 s) on every lock method — request fails fast with `PessimisticLockingFailureException`.
- **Why**: Jakarta EE 11 / Hibernate 6 supports this; default `LockOptions.NO_WAIT` is also possible but 3 s gives room for legit contention.

### Idempotency rewrite on confirm (Plan 02)

- **Before**: `UNIQUE(idempotency_key)` on a nullable column — MySQL allows multiple NULLs, so uniqueness was unreliable for "no key" requests.
- **After**: compound `UNIQUE (confirmation_idempotency_key, status)` — uniqueness enforced **only** when the session flips to `CONFIRMED`. New migration `V31_1`. Application does `findByIdempotencyKeyAndStatus(key, CONFIRMED)` before any DB writes.
- **Why**: matches real-world retry semantics — preview requests without a key must not collide with each other.

### `Money` value object (Plan 06 + propagated to all plans)

- **Before**: raw `BigDecimal` everywhere; scale, rounding mode, and currency were reinvented per call site. `BigDecimal.divide(BigDecimal)` (no scale/mode) crashes at runtime on non-terminating decimals.
- **After**: `Money` is the canonical type in domain + application. `Money.divide(BigDecimal, int scale, RoundingMode mode)` makes scale + mode **mandatory** at compile time. Money uses `java.util.Currency` (ISO 4217 with native `getDefaultFractionDigits()`) — no custom enum.
- **Why**: removes a whole class of bugs (silent scale drift, currency mismatches, division crashes). The harness rule rejects raw `BigDecimal` in `domain/` outside `common/money/`.

### Why not JSR 354 (`javax.money.MonetaryAmount`)? (Plan 06 §1.1)

- Considered `org.javamoney.moneta` and chose a custom `Money` for domain purity (no framework imports in `domain/`), zero new dependencies, and identical ergonomics for our single-currency use case. The record shape is compatible enough that swapping to JSR 354 later would only touch the adapter layer.

### Spring Boot 4 / Jakarta EE 11 namespace (Plan 06 §1.2)

- All imports use `jakarta.persistence.*` (not `javax.persistence.*`).
- `@EnableTransactionManagement` is auto-configured by `spring-boot-starter-data-jpa`; no explicit annotation needed.
- `HibernateJpaAutoConfiguration` moved to its own module (`org.springframework.boot.hibernate.autoconfigure.*`) in Boot 4 — verified against current docs.

### Time-zone correctness in dish-count query (Plan 04 §5)

- **Before**: bucket boundaries computed with `LocalDate.now()` (server TZ).
- **After**: boundaries computed in `system_configuration.business_timezone` (`ZoneId.of(...)`) and bound as `LocalDateTime` to the native query.
- **Why**: a 23:30 Bogota order on the last day of the month landed in the **next** month when the server ran UTC. Fixed by TZ-aware bucketing; covered by a TZ-shift integration test.

### Status filter for "dishes sold" (Plan 04 §5)

- **Before**: undefined.
- **After**: `status <> 'CANCELLED'` (default). Surfaced as `system_configuration.dish_count_status_filter` so business can narrow to `DELIVERED` only without a code change.
- **Why**: cost is incurred at order time, not delivery; counting non-cancelled is the conservative default.

### `BigDecimal` discipline (cross-cutting)

| Rule | Where enforced |
|---|---|
| Never `double` / `float` for money | `Money.of(double, ...)` removed from API surface (Plan 06) |
| Never `BigDecimal.divide(BigDecimal)` without scale + mode | `Money.divide(...)` signature + harness grep (Plan 06 §3, §7.1) |
| Never raw `BigDecimal` in `domain/**` (outside `common/money/`) | Harness grep on `import java.math.BigDecimal;` (Plan 06 §3) |
| Always `RoundingMode.HALF_UP` | `Money` constructor + `MoneyCalculator` helpers (Plan 06 §7) |
| Intermediate scale 4 for weighted averages | `WeightedAverageCalculator` (Plan 01 §4.1) |

---

## Open questions for the user

1. **Currency**: confirm `default_currency = COP` is correct for v1. If multi-currency is on the roadmap, Plan 06's `Money` accommodates it via `java.util.Currency`; only the boundary resolution and the `findByKey("default_currency")` need to grow.
2. **Scheduled zone**: confirm `America/Bogota` as `business_timezone`. Plan 05 reads it from `system_configuration`; Plan 04 uses the same value for order bucketing.
3. **Order status filter**: confirm `non-CANCELLED` for "dishes sold". If business wants `DELIVERED` only, set `dish_count_status_filter = DELIVERED` once shipped.
4. **ShedLock dependency license**: `net.javacrumbs.shedlock` is Apache 2.0; no legal blocker. Add to `build.gradle` in Plan 05's first PR.
5. **Plan 06 rollout**: the refactor touches 7 bounded contexts. Execute as one activity per context, parallelizable, with the harness grep rule **disabled** until the last step (otherwise the build breaks mid-migration).

---

## Acceptance gates (every plan)

- [ ] `./harness/harness.sh` exits 0
- [ ] No `WARN` regressions in harness output
- [ ] No `TODO` / `FIXME` / `System.out.println` left in diff
- [ ] New migrations reference real columns / tables from earlier migrations
- [ ] Every new `@RestController` carries `@Tag` + `@Operation` + `@ApiResponse`
- [ ] Every use case writes audit log where mutating money / stock / pricing
- [ ] `Money` value object used for every monetary domain field (Plan 06)
- [ ] `progress/current.md` reflects what was actually done