# Plan 05 — Monthly Closing Process (Historical Freezing)

> Bounded context: `cost` (extends Plan 04) + a new `scheduled` infrastructure component. Runs on the **1st of every month** to snapshot every product's total dish cost so historical reports remain stable as insumos and fixed costs evolve.

---

## 1. Scope

A scheduled task (cron-style) that, on day 1 of every month at the configured local hour:

1. Reads the previous month's **fixed costs** (Plan 03) and **dishes sold** count (orders table).
2. For **every active product**, computes its dish cost using `CalculateDishCostService` (Plan 04).
3. Persists each result into an **immutable** `historical_product_costs` table keyed by `(product_id, period_year, period_month)`.
4. Marks the closed `monthly_fixed_costs` row as immutable (Plan 03 honors this).
5. Optionally, expires stale `purchase_sessions` (Plan 02) at the same time.

### Out of scope

- Reversal / re-opening (separate "amendment" plan).
- Per-tenant runs (single-tenant today).
- Reporting / BI.

---

## 2. Database schema

### 2.1 `historical_product_costs` (immutable snapshot)

```sql
-- V35__create_historical_product_costs.sql
CREATE TABLE historical_product_costs (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id               BIGINT         NOT NULL,
    period_year              SMALLINT       NOT NULL,
    period_month             TINYINT        NOT NULL,
    -- Inputs frozen at closing time
    material_cost            DECIMAL(12,2)  NOT NULL,
    labor_cost               DECIMAL(12,2)  NOT NULL,
    fixed_cost_allocation    DECIMAL(12,2)  NOT NULL,
    -- Output
    total_cost               DECIMAL(12,2)  NOT NULL,
    -- Snapshot context
    total_fixed_cost_period  DECIMAL(12,2)  NOT NULL,
    dishes_sold_period       BIGINT         NOT NULL,
    cost_per_hour_of_labor   DECIMAL(10,2)  NOT NULL,
    -- Tamper detection (financial-software standard)
    integrity_hash           CHAR(64)       NOT NULL,    -- SHA-256 hex of canonical row payload
    -- Audit
    computed_at              TIMESTAMP(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    closing_job_run_id       BIGINT         NOT NULL,
    closing_strategy         VARCHAR(20)    NOT NULL DEFAULT 'AUTO',   -- AUTO | MANUAL
    UNIQUE KEY uq_hpc_period (product_id, period_year, period_month),
    CONSTRAINT fk_hpc_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_hpc_run     FOREIGN KEY (closing_job_run_id) REFERENCES monthly_closing_runs(id),
    CONSTRAINT chk_hpc_period CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT chk_hpc_year   CHECK (period_year BETWEEN 2000 AND 2100),
    CONSTRAINT chk_hpc_nonneg CHECK (
        material_cost >= 0 AND labor_cost >= 0
        AND fixed_cost_allocation >= 0 AND total_cost >= 0
    )
);

CREATE INDEX idx_hpc_period ON historical_product_costs(period_year, period_month);
CREATE INDEX idx_hpc_product_period ON historical_product_costs(product_id, period_year, period_month);

-- Enforce immutability at the DB level: refuse any UPDATE or DELETE.
-- Standard practice for financial ledger tables.
DELIMITER //
CREATE TRIGGER trg_hpc_no_update
BEFORE UPDATE ON historical_product_costs
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'historical_product_costs is append-only';
END//

CREATE TRIGGER trg_hpc_no_delete
BEFORE DELETE ON historical_product_costs
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'historical_product_costs is append-only';
END//
DELIMITER ;
```

**Integrity hash** — `integrity_hash` is SHA-256 over the canonical pipe-separated string of all money and quantity fields, computed at insert time:

```
canonical = product_id | period_year | period_month
          | material_cost | labor_cost | fixed_cost_allocation | total_cost
          | total_fixed_cost_period | dishes_sold_period | cost_per_hour_of_labor
sha256(canonical)  →  64-char hex
```

A periodic verification job recomputes the hash for every row and reports drift via the existing `NotificationPort`. Cost is ~1 ms per 1000 rows in MySQL.

**Why `TIMESTAMP(3)` for `computed_at`**: millisecond precision lets two consecutive cron ticks be ordered unambiguously if the closing job is ever retried within the same second.

### 2.2 `monthly_closing_runs` (audit / idempotency)

```sql
CREATE TABLE monthly_closing_runs (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_year   SMALLINT NOT NULL,
    target_month  TINYINT  NOT NULL,
    status        VARCHAR(20) NOT NULL,             -- RUNNING | SUCCESS | PARTIAL | FAILED
    started_at    TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    finished_at   TIMESTAMP(3) NULL,
    products_total    INT NULL,
    products_succeeded INT NULL,
    products_failed    INT NULL,
    error_summary  TEXT NULL,
    triggered_by   VARCHAR(40) NOT NULL,           -- CRON | MANUAL_API | ADMIN_REPLAY
    triggered_by_user BIGINT NULL,
    UNIQUE KEY uq_mcr_target (target_year, target_month, triggered_by),
    CONSTRAINT fk_mcr_user FOREIGN KEY (triggered_by_user) REFERENCES users(id),
    CONSTRAINT chk_mcr_status CHECK (status IN ('RUNNING','SUCCESS','PARTIAL','FAILED'))
);
```

### 2.3 `monthly_fixed_costs` immutability flag

```sql
ALTER TABLE monthly_fixed_costs
    ADD COLUMN closed_at TIMESTAMP NULL,
    ADD COLUMN closing_run_id BIGINT NULL,
    ADD CONSTRAINT fk_mfc_run FOREIGN KEY (closing_run_id) REFERENCES monthly_closing_runs(id);
```

When the closing job succeeds, it sets `closed_at` and `closing_run_id`. `UpdateFixedCostService` (Plan 03) rejects edits when `closed_at IS NOT NULL`.

### 2.4 ShedLock table (distributed scheduling)

The job must run **once per cluster**, even when the API is deployed to multiple instances. ShedLock requires this table.

```sql
-- V36__create_shedlock_table.sql
-- Required by net.javacrumbs.shedlock:shedlock-spring with JdbcTemplateLockProvider
CREATE TABLE shedlock (
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

ShedLock dependency (add to `build.gradle`):

```groovy
implementation 'net.javacrumbs.shedlock:shedlock-spring:5.16.0'
implementation 'net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.16.0'
```

---

## 3. Domain layer

```
core/cost/
├── domain/
│   ├── HistoricalProductCost.java
│   ├── MonthlyClosingRun.java
│   ├── ClosingStrategy.java       // enum { AUTO, MANUAL }
│   ├── ClosingStatus.java         // enum { RUNNING, SUCCESS, PARTIAL, FAILED }
│   ├── exception/
│   │   ├── MonthlyClosingAlreadyRunException.java   → 409
│   │   ├── MonthlyClosingInProgressException.java   → 409
│   │   ├── MissingFixedCostForClosingException.java → 422
│   │   └── ClosingRunNotFoundException.java         → 404
│   ├── port/
│   │   ├── input/
│   │   │   └── RunMonthlyClosingUseCase.java
│   │   └── output/
│   │       ├── HistoricalProductCostRepositoryPort.java
│   │       └── MonthlyClosingRunRepositoryPort.java
```

### 3.1 New value objects

```java
public record HistoricalProductCost(
    Long id,
    Long productId,
    YearMonth period,
    Money materialCost,
    Money laborCost,
    Money fixedCostAllocation,
    Money totalCost,
    Money totalFixedCostPeriod,
    long dishesSoldPeriod,
    Money costPerHourOfLabor,
    String integrityHash,                      // SHA-256 hex (64 chars)
    Instant computedAt,                        // millisecond precision
    Long closingJobRunId,
    ClosingStrategy closingStrategy
) {
  // snapshot is conceptually immutable; setters are not exposed

  /** Recomputes and returns the canonical hash for this row. Used by the verifier job. */
  public String recomputeHash() {
    String canonical = productId + "|"
                     + period.year() + "|" + period.month() + "|"
                     + materialCost.amount().toPlainString() + "|"
                     + laborCost.amount().toPlainString() + "|"
                     + fixedCostAllocation.amount().toPlainString() + "|"
                     + totalCost.amount().toPlainString() + "|"
                     + totalFixedCostPeriod.amount().toPlainString() + "|"
                     + dishesSoldPeriod + "|"
                     + costPerHourOfLabor.amount().toPlainString();
    return IntegrityHasher.sha256Hex(canonical);
  }
}

public record MonthlyClosingRun(
    Long id,
    YearMonth target,
    ClosingStatus status,
    Instant startedAt,
    Instant finishedAt,
    Integer productsTotal,
    Integer productsSucceeded,
    Integer productsFailed,
    String errorSummary,
    String triggeredBy,
    Long triggeredByUser
) {}
```

`IntegrityHasher` lives in `core/common/integrity/` and is a thin wrapper around `MessageDigest.getInstance("SHA-256")`. Domain-side only depends on `java.security.MessageDigest` and `java.util.HexFormat` (Java 17+).

---

## 4. Application layer — `RunMonthlyClosingService`

```
MonthlyClosingRun run(RunMonthlyClosingCommand cmd)
```

```java
public record RunMonthlyClosingCommand(
    YearMonth target,           // year+month to close
    boolean forceReplay,         // false for cron, true for manual replay only
    Long performedBy             // null for cron
) {}
```

Flow:

1. **Idempotency guard**:
   - If a `MonthlyClosingRun(target, status=SUCCESS)` exists and `!forceReplay` → `MonthlyClosingAlreadyRunException` 409.
   - If a run exists with `status=RUNNING` and started < 1h ago → `MonthlyClosingInProgressException` 409.
2. **Pre-flight checks**:
   - `monthly_fixed_costs(target.year, target.month)` must exist → else `MissingFixedCostForClosingException` 422.
   - `system_configuration.cost_per_hour_of_cooking_labor` must exist → else `MissingLaborCostConfigurationException` 422.
3. **Create `MonthlyClosingRun(status=RUNNING)`** (insert).
4. **Fetch inputs**:
   - `List<Product> activeProducts = productRepositoryPort.findAllActive();`
   - `totalFixed = fixedCosts.totalFixedCost`
   - `dishesSold = orderCountPort.countDishesSoldIn(target)`
   - `laborRate = systemConfig.requireByKey("cost_per_hour_of_cooking_labor")`
5. **Per-product loop** (in batches of 50 to bound memory):
   - For each `Product`:
     - `DishCost cost = calculateDishCostService.calculateForPeriod(productId, target);`
     - `HistoricalProductCost snap = toSnapshot(productId, target, cost, totalFixed, dishesSold, laborRate, runId);`
     - `historicalProductCostRepositoryPort.save(snap);` — wrapped in try/catch.
     - On `DataIntegrityViolationException` (UNIQUE) → product already snapshotted → skip + increment `productsSucceeded` (idempotent replay).
6. **Persist immutability**:
   - `monthly_fixed_costs(target.year, target.month)` → set `closed_at = NOW()`, `closing_run_id = runId`.
7. **Mark run**:
   - `status = (productsFailed == 0 ? SUCCESS : PARTIAL)`
   - `finished_at = NOW()`.
   - If `forceReplay == false` and `productsFailed > 0` → emit `NotificationPort` warning (admin sees in real-time).
8. **Housekeeping**:
   - `expirePurchasePreviewsService.expireDue()` (Plan 02).
9. Return the `MonthlyClosingRun`.

### 4.1 Failure semantics

- The whole batch runs inside a `@Transactional` boundary **per product**, not for the entire run. Reason: 1000 products × 1 failure should not roll back 999 successes.
- A `PARTIAL` status is acceptable — admin can replay the missing products via `forceReplay=true`.
- `FAILED` is reserved for pre-flight failures (missing fixed cost, etc.).

### 4.2 Manual replay endpoint

```
POST /api/v1/cost/monthly-closing/runs
{
  "year": 2026,
  "month": 6,
  "forceReplay": true
}
```

Returns the resulting `MonthlyClosingRun`. Admin only (role check).

---

## 5. Infrastructure — Scheduled trigger

```
infraestructure/scheduled/
├── MonthlyClosingScheduler.java       // @Scheduled + @SchedulerLock
├── SchedulingConfig.java              // @EnableScheduling + @EnableSchedulerLock + LockProvider
├── MonthlyClosingProperties.java      // @ConfigurationProperties (dynamic cron)
└── ReadMonthlyClosingSettings.java    // queries system_configuration if present
```

### 5.1 ShedLock configuration (Spring-recommended pattern)

Spring's `@Scheduled(cron = "${...}")` does **not** support SpEL placeholders directly. Use `SchedulingConfigurer` to load the cron dynamically from `application.yml`, then protect the method with ShedLock's `@SchedulerLock` so only one instance in the cluster runs the job.

```java
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT6H")
public class SchedulingConfig {

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    // Uses the shedlock table created in V36
    return new JdbcTemplateLockProvider(dataSource);
  }

  @Bean
  public TaskScheduler taskScheduler(MonthlyClosingProperties props) {
    // Pool size must be >= number of distinct scheduled tasks
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(2);
    scheduler.setThreadNamePrefix("rms-monthly-closing-");
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(30);
    scheduler.initialize();
    return scheduler;
  }
}
```

### 5.2 Scheduled task

```java
@Component
public class MonthlyClosingScheduler {

  private final RunMonthlyClosingUseCase useCase;
  private final SystemConfigurationRepositoryPort configPort;
  private final TaskScheduler scheduler;
  private final MonthlyClosingProperties props;

  // NOTE: cron cannot be a property placeholder on @Scheduled itself.
  // We register the trigger programmatically in afterPropertiesSet().
  private CronTrigger trigger;

  @Override
  public void afterPropertiesSet() {
    this.trigger = new CronTrigger(props.getCron(), ZoneId.of(props.getZone()));
  }

  public void schedule() {
    scheduler.schedule(this::closePreviousMonth, trigger);
  }

  @SchedulerLock(
      name = "rmsMonthlyClosing",
      lockAtMostFor = "PT23H",   // safety: never longer than 23h
      lockAtLeastFor = "PT5M"    // prevent fast re-run if clock skew
  )
  public void closePreviousMonth() {
    LockAssert.assertLocked();
    YearMonth target = YearMonth.of(LocalDate.now(zone(props.getZone()))).previousMonth();
    try {
      MonthlyClosingRun run = useCase.run(new RunMonthlyClosingCommand(target, false, null));
      log.info("Monthly closing completed: target={}, status={}, succeeded={}, failed={}",
               target, run.status(), run.productsSucceeded(), run.productsFailed());
    } catch (Exception ex) {
      log.error("Monthly closing failed: target={}", target, ex);
      // do NOT rethrow — cron must not crash the scheduler thread
    }
  }
}
```

Defaults: cron `0 0 2 1 * *` = **02:00 on day 1 of every month**, timezone `America/Bogota`. Both overridable via `application.yml` and `system_configuration` table.

**Concurrency guarantees**:
- Two instances fire at the same time → ShedLock lets only one acquire `shedlock.name = 'rmsMonthlyClosing'`. The other gets `LockProvider` exception (caught silently inside ShedLock, no re-run).
- Manual API trigger + cron → use case layer's UNIQUE `(target_year, target_month)` + status check is the second line of defense (see § 7).

### 5.3 Configuration surface (`application.yml`)

```yaml
spring:
  task:
    scheduling:
      pool:
        size: 2
      thread-name-prefix: "rms-scheduling-"

rms:
  cost:
    monthly-closing:
      enabled: true
      cron: "0 0 2 1 * *"        # sec min hour day month dayOfWeek
      zone: "America/Bogota"
      batch-size: 50
      lockAtMostFor: "PT23H"     # ShedLock max duration
      lockAtLeastFor: "PT5M"     # ShedLock min hold
```

`@EnableScheduling` is wired in `SchedulingConfig`. Without it, `@Scheduled` methods are silently ignored.

---

## 6. REST API surface

Base path: `/api/v1/cost/monthly-closing`. `@Tag(name = "Monthly Closing")`.

| Method | Path | Purpose | Status |
|---|---|---|---|
| `POST` | `/runs` | Trigger a manual run (admin) | 200 / 409 / 422 |
| `GET`  | `/runs?year=&month=` | List runs for a period | 200 |
| `GET`  | `/runs/{id}` | Get a single run with details | 200 / 404 |
| `GET`  | `/products/{id}/cost/history?year=&month=` | Fetch historical snapshots for a product | 200 |
| `GET`  | `/products/{id}/cost/history/latest` | Most recent snapshot | 200 / 404 |

### Response (snapshot)

```json
// GET /products/42/cost/history?year=2026&month=6
{
  "productId": 42,
  "productName": "Hamburguesa Especial",
  "period": { "year": 2026, "month": 6 },
  "materialCost":        { "amount": 5.20, "currency": "COP" },
  "laborCost":           { "amount": 2.40, "currency": "COP" },
  "fixedCostAllocation": { "amount": 1.05, "currency": "COP" },
  "totalCost":           { "amount": 8.65, "currency": "COP" },
  "context": {
    "totalFixedCostPeriod": { "amount": 8500000.00, "currency": "COP" },
    "dishesSoldPeriod": 8095,
    "costPerHourOfLabor":   { "amount": 12000.00, "currency": "COP" },
    "closingRunId": 17,
    "computedAt": "2026-07-01T02:00:14Z"
  }
}
```

---

## 7. Concurrency model

| Concern | Handling |
|---|---|
| Cron and manual run collide | UNIQUE `(target_year, target_month, triggered_by)` + status check (`RUNNING`) → second loses with 409 |
| Cron double-fires (clustered deploy) | DB UNIQUE + status check → only one wins |
| Run mid-flight when a purchase updates a variant used by a snapshot | Snapshot is **already frozen** at insert time → no impact |
| `closed_at` set during fix-cost update race | `PESSIMISTIC_WRITE` lock on `monthly_fixed_costs(target.year, target.month)` inside the closing job |
| `@Transactional` scope | Per-product (see § 4.1) — partial failure tolerated |

---

## 8. Sequence — cron run

```
Scheduler                          RunMonthlyClosingService             DB
  |  cron tick 02:00 day 1          |                                   |
  |--------------------------------->|                                   |
  |                                  | idempotency check                 |
  |                                  | INSERT monthly_closing_runs       |
  |                                  |---------------------------------->|
  |                                  |                                   |
  |                                  | for each active product:          |
  |                                  |   calculateDishCost               |
  |                                  |   INSERT historical_product_costs |
  |                                  |---------------------------------->|
  |                                  |                                   |
  |                                  | UPDATE monthly_fixed_costs        |
  |                                  |   SET closed_at, closing_run_id   |
  |                                  |---------------------------------->|
  |                                  |                                   |
  |                                  | UPDATE monthly_closing_runs       |
  |                                  |   SET status=SUCCESS/PARTIAL      |
  |                                  |---------------------------------->|
  |                                  | expire stale purchase_sessions    |
  |                                  | notify (if PARTIAL)               |
```

---

## 9. Edge cases

| Case | Handling |
|---|---|
| No `monthly_fixed_costs` for target period | `MissingFixedCostForClosingException` 422; run marked `FAILED` |
| `dishesSold == 0` | Each `HistoricalProductCost` stored with `fixed_cost_allocation = 0`, `total_cost = material + labor` |
| Product has no recipe at closing time | Snapshot still created with `material_cost = 0`, `total_cost = labor + 0`; flagged in `error_summary` |
| Cron tick missed (downtime) | Admin replays via `POST /runs?forceReplay=false` next day → succeeds for the missed period |
| Manual run for a future period | Allowed (e.g., test); UNIQUE prevents duplicates |
| Closing job runs while a purchase is being confirmed (Plan 02) | `monthly_fixed_costs.closed_at` set only after snapshots inserted; in-flight purchase affects **current** period, not the closing one |
| Time zone drift between server and `business_timezone` config | `YearMonth.of(LocalDate.now(zone))` always uses the configured zone; never `LocalDate.now()` |
| `monthly_closing_day` / `monthly_closing_hour` config keys | Read at scheduler startup; defaults from `application.yml`; if changed mid-month, restart or wait for next tick |

---

## 10. Tests

| Test class | Coverage |
|---|---|
| `DishCostFormulaTest` (already from Plan 04) | Pure math used here too |
| `RunMonthlyClosingServiceTest` | Happy path; partial failure (one product throws) → PARTIAL; missing fixed cost → 422 |
| `RunMonthlyClosingServiceTest` | Idempotency: replay with `forceReplay=true` overwrites; without, 409 |
| `MonthlyClosingSchedulerTest` | Cron calls use case with `previousMonth()` |
| `HistoricalProductCostRepositoryIT` | UNIQUE constraint enforced |
| `FixedCostUpdateRejectedAfterClosingIT` | Plan 03 update endpoint returns 409 when `closed_at IS NOT NULL` |
| `E2E IT` | Spin fixed cost + orders → run closing → fetch history → fixed cost update returns 409 |

---

## 11. Acceptance criteria

- [ ] `V35` migration creates `historical_product_costs` + `monthly_closing_runs`; `monthly_fixed_costs` gains `closed_at`/`closing_run_id`.
- [ ] `V36` migration creates `shedlock` table.
- [ ] ShedLock dependency added to `build.gradle` and `@EnableSchedulerLock` configured.
- [ ] Cron fires at `02:00 day 1` in `America/Bogota` by default and runs `RunMonthlyClosingService`.
- [ ] **Cluster test**: spin up two app instances pointing at the same DB → only one runs the closing job.
- [ ] After a run, every active product has exactly one `historical_product_costs` row for the target period.
- [ ] `monthly_fixed_costs(target).closed_at` is set; Plan 03 update endpoint returns 409 thereafter.
- [ ] Manual replay via `POST /runs` works and is idempotent.
- [ ] Money values use `Money` value object (Plan 06).
- [ ] `./harness/harness.sh` exits 0.

---

## 12. Risks / follow-ups

- **Clustered cron**: only one instance should run the closing job per period. If the project ever deploys multi-instance, switch the trigger from `@Scheduled` to a distributed lock (e.g., ShedLock) gated by the same `monthly_closing_runs` UNIQUE — the lock will be enforced anyway, but a DB lock avoids wasted work.
- **Replay vs amend**: `forceReplay=true` overwrites snapshots. If business wants non-destructive amendments, add a separate `historical_product_cost_amendments` table later.
- **Performance**: closing 1000 products × full recipe expansion + fixed cost + order count query is ~1s per product worst case. Batch + async if it grows.
- **Notification of PARTIAL**: emit on the same `NotificationPort` already used for inventory events so admin dashboard gets a real-time banner.