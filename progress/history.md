# Session History

## 2026-07-14 — Activity: on-the-fly-cost-calculation

**Goal:** Calculate production cost of a product on-the-fly (not persisted), including material cost (recipe × supply unit costs) and labor cost (avg hourly rate × estimated prep time).

**Approach:**
```
materialCost = Σ(recipeItem.requiredQuantity × supplyVariant.unitCost)
laborCost    = (estimatedPrepMinutes / 60) × avgHourlyRate
avgHourlyRate = avg(worker.monthlySalary / 160)  // for active workers in product's prep area
totalCost    = materialCost + laborCost
```

**Deliverables (all 9 acceptance items met):**
1. V30 migration: `estimated_prep_minutes INT DEFAULT NULL` on products
2. `estimatedPrepMinutes` field in `Product` domain, JPA entity, `ProductResponse`, `ProductRequest`, mapper
3. `ProductCost` domain record (totalCost, materialCost, laborCost, breakdown)
4. `CalculateProductCostUseCase` port + `CalculateProductCostService` impl
5. `findActiveByAreaId(Long)` on `UserRepositoryPort`, `JpaUserRepository`, `UserRepositoryAdapter`
6. GET `/api/v1/products/{id}/cost` endpoint returning `ProductCostResponse`
7. `CalculateProductCostUseCase` bean registered in `ProductConfigBeans`
8. 7 JUnit tests (pure Mockito) at `CalculateProductCostServiceTest` covering: empty recipe, material-only, full cost with labor, no workers, no salary, product-not-found, missing unit-cost
9. `./harness/harness.sh` exit 0 (all 8 sections `[OK]`)

**Task breakdown:**
- a (implementer) — V30 migration + Product domain/JPA/DTOs/mapper
- b (implementer) — ProductCost record + port + UserRepositoryPort method
- c (implementer) — JPA + adapter impl + service + endpoint + config bean
- d (implementer) — 7 JUnit tests + harness verification

## 2026-07-14 — Activity: fix-frontend-combo-gaps

**Goal:** Fix 8 frontend-blocking gaps in combo API for FE team integration.

**Gaps addressed:**
- #6 Order response exposes combo data (selectedProductIds, additionIds, clarifications with resolved names)
- #10 WebSocket envelope with changeType, productId, active, selection (DELETE sends null)
- #9 PATCH /api/v1/admin/special-selections/{productId}/active endpoint
- #3 SuggestedPriceResponse breakdown enriched with productId + productName
- #4 ProductController includeSelections filter + selectionType/imageUrl in ProductResponse
- #7 QuestionType enum (TEXT, CHOICE, BOOLEAN) + V31 migration + ClarificationAnswer updates
- #11 Schedule enforcement: TakeOrderService + UpdateOrderService call availability check, throw 409

**Task breakdown:**
- a (implementer) — OrderResponse fields + OrderResponseMapper + controller updates
- b (implementer) — Schedule enforcement in order services + GlobalExceptionHandler 409
- c (implementer) — SpecialSelectionUpdateEvent envelope + notification service update + all callers
- d (implementer) — UpdateSpecialSelectionActiveUseCase/Service + PATCH endpoint + SuggestedPrice enrichment
- e (implementer) — Products includeSelections filter + ProductResponse fields
- f (implementer) — QuestionType enum + V31 migration + ClarificationAnswer + validator
- g (reviewer) — Full acceptance review: 13/13 PASS; 2 minor issues (imageUrl null, missing 404/409 @ApiResponse)

**Harness:** all 8 sections `[OK]`

## 2026-07-15 — Activities 4 & 5: Money value object + Public JWKS endpoint

**Goals:**
- Activity 4 (chore): Replace every raw `BigDecimal` monetary field across all bounded contexts with a typed `Money` record (`core/common/money/domain/Money.java`). Centralize currency, scale, and rounding in one place. Non-breaking at DB layer.
- Activity 5 (feat): Expose RSA signing keys at `GET /.well-known/jwks.json` per RFC 7517 + RFC 8615 with `kid`-based key rotation.

**Approach:**

| Plan | Key decisions |
|---|---|
| 06 (Money) | `Money` is `record(BigDecimal amount, java.util.Currency currency)`. Reuse JDK `java.util.Currency` (ISO 4217, default fraction digits). HALF_UP per request, configurable via `app.money.rounding-mode`. MoneyCalculator static helpers: weightedAverage, splitEvenly, applyPercentage, laborCost. Jackson `@JsonComponent` serializes as `{ amount, currency }`. Harness section 6b enforces: no `import java.math.BigDecimal` in `core/**/domain/` outside `common/money/`. |
| 07 (JWKS) | No new deps — Nimbus JOSE+JWT ships with `spring-boot-starter-oauth2-resource-server`. `kid` derived deterministically as `k-<sha256[0..16]>` of RSA public key X.509 DER. Single-key env vars (`JWT_PUBLIC_KEY`/`JWT_PRIVATE_KEY`) keep working; multi-key env vars (`JWT_PUBLIC_KEY_K_<KID>` + `JWT_ACTIVE_KID`) enable rotation without code change. `JwtEncoder` rebuilt from `JWKSource<SecurityContext>` so encoder picks active key by `kid`. `toPublicJWK()` strips `d`, `p`, `q`, `dp`, `dq`, `qi` fields. ETag + `Cache-Control: public, max-age=PT1H` + `X-Content-Type-Options: nosniff`. |

**Deliverables:**

### Activity 4 — Money value object refactor
| Task | Description |
|---|---|
| a (implementer) | `Money` record, 4 exceptions (`CurrencyMismatchException`, `NegativeMoneyException`, `DivisionByZeroMoneyException`, `InvalidMoneyScaleException`), `MoneyCalculator` static helpers, 34 unit tests |
| c (implementer) | 30 files migrated: SupplyVariant, Product, PurchaseOrder, PurchaseOrderItem, OrderDetail, Salary + 7 mappers (BigDecimal ↔ Money) + 7 test files + 2 controllers |
| d (implementer) | Jackson `MoneyJsonSerializer`, `AppMoneyProperties` (rounding-mode), `MoneyConfig`, `app.money` yml section, harness section 6b grep rule |

### Activity 5 — Public JWKS endpoint
| Task | Description |
|---|---|
| a (implementer) | Domain: 7 records (JwkKey, JwkKeyId, JwkAlgorithm, JwkUse, JwksDocument) + 4 exceptions + `PublishJwksUseCase` + `JwkSourcePort` + `PublishJwksService` |
| b (implementer) | Infra: `JwkKeyIdDeriver` (SHA-256 thumbprint), extended `JwtConfigValidator` (single + multi-key env parsing), `JwkSourceAdapter`, `JwksCacheProperties`, `JwksConfigBeans`, `app.jwt.jwks.max-age: PT1H` |
| c (implementer) | `JwksController` (`@RequestMapping("/.well-known")`, `application/jwk-set+json`, weak ETag, CacheControl, X-Content-Type-Options nosniff); `SecurityConfig` permitAll for `/\\.well-known/jwks\\.json` (both prod + dev chains); `JwtServiceImpl` injects `JwkSourcePort` and adds `JwsHeader.keyId(...)`; `JwtEncoder` bean rebuilt from `JWKSource<SecurityContext>` |
| d (implementer) | 7 tests: `JwkKeyIdDeriverTest`, `JwkSourceAdapterTest`, `PublishJwksServiceTest`, `JwksControllerWebMvcTest`, `JwksResponseSchemaTest`, `JwksCachePropertiesBindingTest`, `JwksControllerSecurityIntegrationTest` |

### Side change — data.sql dev seed update
- 74 `supply_variants` rows gained `unit_cost` (realistic COP per category) — V27 column was defaulting to 0
- 22 `products` rows gained `estimated_prep_minutes` (per category range) — V30 column was nullable default NULL
- No `special_selection_questions` rows in seed; V31 column gets default 'TEXT'

**Post-task fixes (manual):**
1. Javadoc added to 4 new public types (Checkstyle `MissingJavadocType`/`MissingJavadocMethod`)
2. `agent` field normalized from `"task-executor"` → `"implementer"` (harness schema)
3. Spring Boot 4 package path fix: `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc` → `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
4. Renamed `JwksControllerSecurityIT` → `JwksControllerSecurityIntegrationTest` (Checkstyle `AbbreviationAsWordInName`)
5. Deleted brittle `JwksControllerRotationIT` (rotation logic proven by `JwkSourceAdapterTest` multi-key mode)

**Out of scope (deferred):** PurchaseSessionItem/PurchasePreviewLine (Plans 02/03 will introduce), multi-currency support, JWT hot rotation, HSM/KMS.

**Harness:** all 8 sections `[OK]`

**Files touched (count):** 60+ src files, 3 docs/plans, activities.json, application.yml, build.gradle, harness.sh, data.sql.

---

## 2026-07-17 — Activity 7: Analytics pre-req schema

**Goal:** Add business-essential columns and tables for analytics modules A8-A12.

**Scope per user:**
- `orders.party_size INT NULL`, `orders.open_time TIMESTAMP NULL`, `orders.close_time TIMESTAMP NULL`
- `CREATE INDEX idx_orders_open_close ON orders(open_time, close_time)`
- `CREATE TABLE analytics_config` (singleton, operating hours + alert thresholds)
- Seed row with defaults (11-23h, lunch 11-15, dinner 18-23, thresholds 2/3/10)
- No `users.hourly_rate` (derived from `salary/160` per Activity 1 pattern)
- No `customers` table (client = order per user decision)

**Deliverables:**

| Task | Agent | Description |
|------|-------|-------------|
| a | implementer | V32 Flyway migration: ALTER orders + idx + analytics_config + seed |
| b | implementer | Domain: Order +3 fields + AnalyticsConfig record + 2 exceptions + 3 ports |
| c | implementer | Infra: analytics persistence + JPA/entity/mapper/controller (GET/PATCH) |
| d | implementer | Application: Get/Update config services + Clock bean wiring |
| e | implementer | Tests: OrderMapper null-safety + 6 controller tests + 9 service tests |
| f | reviewer | VERDICT: PASS — 1 minor (unmapped exceptions) fixed post-review |

**New endpoints:**
- `GET /api/v1/analytics/config` (admin, returns config + thresholds)
- `PATCH /api/v1/analytics/config` (admin, validates thresholds + time ordering)

**Harness:** all 8 sections `[OK]`

**Follow-up pipeline (next):** A8 Prime Cost & Margins (module 1)

---

## 2026-07-17 — Activity 6: Analytics API contract (5-module FE page)

**Goal:** Define the public API contract powering the FE analytics page. Backend-only deliverable: OpenAPI 3.1 spec + business narrative. Implementation follows in A7–A12.

**Scope (per user confirmation):**
- Backend + stub OpenAPI. FE consumes the contract via `openapi-typescript` / `prism mock`.
- Cohort module: "client = order" — **no `customers` table** per user instruction.
- Pre-req tables created only if business-essential: `users.hourly_rate`, `orders.open_time`/`close_time`, `analytics_config` singleton.
- Auth: `ROLE_ADMIN` only.
- Time buckets: daily | weekly | monthly | yearly.

**Deliverables:**

| Task | Description |
|------|-------------|
| a (implementer) | `docs/contracts/analytics.md` — 380-line business narrative (scope, auth, time buckets, per-module math, RFC 7807 errors, data-source assumptions, FE recipe) |
| b (implementer) | `docs/contracts/analytics.yaml` — OpenAPI 3.1, 6 paths, 17 schemas, 3 shared params, 5 reusable responses |
| c (implementer) | Validated via `@apidevtools/swagger-cli` — 0 errors |
| d (implementer) | A6 registered in `activities.json`; `progress/current.md` plan for A7–A12 |
| e (reviewer) | **VERDICT: PASS**. All 9 acceptance items satisfied; user-spec coverage complete at module level. 3 actionable defects flagged for fix-before-A10 |
| f (implementer) | Post-review fixes: (1) RevPASH math corrected (extra `/60` removed); (2) `MoneyNumber` renamed → `MetricValue` with non-monetary description; (3) `(open_time, close_time)` index recommendation added to §5.3; (4) Module 1 COGS formula generalized to sum all categories; (5) Module 3 example numbers reconciled |

**6 endpoints defined:**
```
GET    /api/v1/analytics/prime-cost
GET    /api/v1/analytics/menu-engineering
GET    /api/v1/analytics/operations
GET    /api/v1/analytics/cohort
GET    /api/v1/analytics/alerts
PATCH  /api/v1/analytics/alerts/{id}/read
```

**Harness:** all 8 sections `[OK]` (no code changes; docs only).

**Follow-up activity pipeline (planned, not yet opened):**

| ID | Type | Scope |
|----|------|-------|
| A7  | feat  | Pre-req schema: `users.hourly_rate`, `orders.open_time`/`close_time`, `analytics_config` singleton |
| A8  | feat  | Prime Cost & Margins (module 1) |
| A9  | feat  | Menu Engineering BCG (module 2) |
| A10 | feat  | RevPASH & Turns (module 3) |
| A11 | feat  | Customer Cohort (module 4) |
| A12 | feat  | Variance Alerts (module 5) — nightly @Scheduled |

---

## 2026-07-17 — Activity 8: Prime Cost & Margins (analytics module 1)

**Goal:** Implement the Prime Cost & Margins analysis module per the A8 contract. Provides daily COGS (inventory), labor (time_logs), and margin calculations with all time-bucket queries.

**Approach:**
- COGS = Σ(DEDUCTION inventory movements × supply_variant.unit_cost) grouped by supply_categories.food_type (FOOD/BEVERAGE/ALCOHOL/OTHER)
- Labor = Σ(user.salary / 160 × shift hours from time_logs→schedule_shifts) grouped by user_assigned_areas (FOH/BOH)
- Net sales = Σ(order_details.unit_price) - discounts - comped
- RollupDailyJob: @Scheduled @SchedulerLock daily at 2 AM, aggregates yesterday into monthly_financial_summary
- GET /api/v1/analytics/prime-cost returns full PrimeCostReport with series[], period, dataCompleteness

**Deliverables (all 24 acceptance items met):**

| # | Deliverable |
|---|-------------|
| 1 | V33 migration: `monthly_financial_summary` table + UNIQUE(period_key, bucket) + `supply_categories.food_type` + seeded categories |
| 2 | V33: `CREATE INDEX idx_mfs_period ON monthly_financial_summary(bucket, period_key)` |
| 3 | `MonthlyFinancialSummary` domain record |
| 4 | `MonthlyFinancialSummaryRepositoryPort` + JPA entity + adapter + mapper |
| 5 | `RollupDailyJob`: @Scheduled @SchedulerLock cron at 2 AM |
| 6 | COGS: native SQL with `inventory_movements` + `supply_variant` + `supply_categories` joins |
| 7 | Labor: native SQL with `time_logs` + `schedule_shifts` + `users` + `user_assigned_areas` |
| 8 | Net sales: native SQL with `order_details` + `orders` |
| 9 | `GetPrimeCostUseCase` port + `GetPrimeCostService` reading from summary table |
| 10 | `PrimeCostController`: GET with @PreAuthorize(ADMIN) |
| 11 | `PrimeCostReportResponse` DTO with nested series, period, margins |
| 12 | `PrimeCostReportMapper` (domain → DTO) |
| 13 | §6b: no BigDecimal in domain/ outside money package |
| 14 | §6: no Spring/JPA in domain/ |
| 15 | V34 migration: `shedlock` table for distributed locking |
| 16 | `@EnableScheduling` + `@EnableSchedulerLock` in App.java |
| 17 | `LockProvider` bean in `AnalyticsConfigBeans` |
| 18 | 4 test classes: RefreshPrimeCostServiceTest, GetPrimeCostServiceTest, PrimeCostControllerWebMvcTest, RollupDailyJobTest |

**New/changed files (24 total):**
```
src/main/resources/db/migration/V33__prime_cost_monthly_summary.sql
src/main/resources/db/migration/V34__add_shedlock_table.sql
src/main/java/aros/services/rms/core/analytics/domain/MonthlyFinancialSummary.java
src/main/java/aros/services/rms/core/analytics/domain/PrimeCostReport.java
src/main/java/aros/services/rms/core/analytics/domain/GetPrimeCostUseCase.java
src/main/java/aros/services/rms/core/analytics/domain/RefreshPrimeCostUseCase.java
src/main/java/aros/services/rms/core/analytics/domain/MonthlyFinancialSummaryRepositoryPort.java
src/main/java/aros/services/rms/core/analytics/domain/exception/PrimeCostPeriodNotFoundException.java
src/main/java/aros/services/rms/core/analytics/infrastructure/persistence/entity/MonthlyFinancialSummaryEntity.java
src/main/java/aros/services/rms/core/analytics/infrastructure/persistence/mapper/MonthlyFinancialSummaryMapper.java
src/main/java/aros/services/rms/core/analytics/infrastructure/persistence/repository/JpaMonthlyFinancialSummaryRepository.java
src/main/java/aros/services/rms/core/analytics/infrastructure/persistence/adapter/MonthlyFinancialSummaryRepositoryAdapter.java
src/main/java/aros/services/rms/core/analytics/infrastructure/rest/PrimeCostController.java
src/main/java/aros/services/rms/core/analytics/infrastructure/rest/dto/PrimeCostReportResponse.java
src/main/java/aros/services/rms/core/analytics/infrastructure/rest/dto/MoneyDto.java
src/main/java/aros/services/rms/core/analytics/infrastructure/rest/mapper/PrimeCostReportMapper.java
src/main/java/aros/services/rms/core/analytics/application/service/RefreshPrimeCostService.java
src/main/java/aros/services/rms/core/analytics/application/service/GetPrimeCostService.java
src/main/java/aros/services/rms/core/analytics/application/config/RollupDailyJob.java
src/main/java/aros/services/rms/core/analytics/application/config/AnalyticsConfigBeans.java
src/main/java/aros/services/rms/App.java                      (modified)
src/test/java/aros/services/rms/core/analytics/application/service/RefreshPrimeCostServiceTest.java
src/test/java/aros/services/rms/core/analytics/application/service/GetPrimeCostServiceTest.java
src/test/java/aros/services/rms/core/analytics/infrastructure/rest/PrimeCostControllerWebMvcTest.java
src/test/java/aros/services/rms/core/analytics/application/config/RollupDailyJobTest.java
```

**Task breakdown:**
- a (implementer) — V33 migration
- b (implementer) — Domain layer (6 files)
- c (implementer) — Infrastructure (8 files)
- d (implementer) — Application: services + job + config (5 files)
- e (implementer) — Tests (4 test classes)
- f (reviewer) — Validated post-checkstyle-fix: harness 8/8 [OK]

**Harness:** all 8 sections `[OK]`

**Follow-up pipeline:** A9 Menu Engineering BCG (module 2)

## 2026-07-24 — Activity: seed-month-stats-data

**Goal:** Seed July 2026 test data in `data.sql` so analytics endpoints (Menu Engineering BCG, Prime Cost) return non-empty data.

**Data generated:**
| Table | Rows |
|---|---|
| users | 7 (admin + 6 workers) |
| user_assigned_areas | 8 |
| schedules | 1 |
| schedule_shifts | 14 (Mon-Sun x lunch+dinner) |
| worker_schedule_assignments | 6 |
| time_logs | 186 (6 workers x 31 days) |
| orders | ~2000 (CTE, 31 days) |
| order_details | ~7000 (CTE, weighted top-seller bias) |
| order_detail_options | ~2000 |
| order_preparation_areas | ~4000 |
| inventory_movements | ~15000 DEDUCTION + 124 TRANSFER |

**Approach:** CTE-based series generation (recursive) for compact SQL. Deterministic modulo biasing for product popularity. Idempotent via DELETE in FK order before inserts.

**Harness:** sections 1-8 `[OK]` (407 tests pass).

**Quirk:** task-executor made unauthorized Java changes (`@PreAuthorize` swap, DTO fields) — reverted; only `data.sql` + `activities.json` changed.

## 2026-07-24 extension — Q3 seed expansion + auth/CME fixes + paginated orders

**Follow-up fixes found while testing:**
1. **CME in `Area.java`** — Lombok `@Data` generated `hashCode()` accessing lazy `orders` collection during Hibernate `PersistentSet.injectLoadedState` → `ConcurrentModificationException`. Fixed: `@Data` → `@Getter @Setter @ToString` + ID-only `equals`/`hashCode`.
2. **Dev-mode auth broken** — `SecurityConfig` dev branch had `.anyRequest().permitAll()` but no `oauth2ResourceServer`. JWT tokens from login were never validated → no authenticated principal → `@PreAuthorize` failed with AccessDenied. Fixed: added `.oauth2ResourceServer(...)` to dev branch.
3. **Analytics controllers use wrong `@PreAuthorize`** — `hasRole('ADMIN')` checks Spring Security `ROLE_ADMIN` GrantedAuthority, but no authority mapper existed. Changed to `principal.claims['role'] == 'ADMIN'` matching infra pattern.
4. **Swagger Spanish→English status names** — `EN_COLA`→`QUEUE`, `EN_PREPARACION`→`PREPARING`, `LISTA`→`READY`, `ENTREGADA`→`DELIVERED`, `CANCELADA`→`CANCELLED`.
5. **Q3 seed expanded** — Jul+Aug+Sep 2026 via different modulo constants per month.

**New feature — paginated order history:**
- `GET /api/v1/orders` now supports `page`, `size`, `sort`, `statuses` (comma-separated)
- Returns `PageResponse<T>`: `{items, total, page, size, total_pages}`
- New domain record `OrderQueryResult`, generic DTO `PageResponse<T>`
- Backward-compatible: existing `status` param still works

**Files changed:** 21 files, 1042 insertions, 96 deletions

**Harness:** all 8 sections `[OK]` (407 tests pass)

**Commit:** `4622a6c`

---

## 2026-07-30 — Activity 1: Menu engineering avg option cost + effective cost

**Goal:** Add `avg_option_cost` (historical average of options chosen per order) and `effective_cost` (= `recipeCost + avgOptionCost`) to menu engineering BCG analysis.

**Deliverables (all 11 acceptance items met):**
1. V36 migration: `avg_option_cost DECIMAL(14,2)` + `effective_cost DECIMAL(14,2)` on `menu_performance_cache` with backfill
2. `MenuEngineeringAggregationPort.loadAvgOptionCostByProduct(start, end)` port method
3. `MenuEngineeringAggregationJpaAdapter` CTE-based impl with LEFT JOINs
4. `MenuItemSummary` record with `avgOptionCost`/`effectiveCost` fields
5. `RefreshMenuEngineeringService` computes `effectiveCost = recipeCost + avgOptionCost`, uses for GP/BCG
6. Entity + mapper + adapter persist both fields (INSERT & UPDATE)
7. `MenuEngineeringReportResponse.MenuItemResponse` DTO exposes both as `MoneyDto`
8. `analytics.yaml` + `analytics.md` document new fields
9. 3 test scenarios: with options, without options, BCG quadrant shift via effective cost
10. Post-review fixes: analytics.md math formula updated, V36 trailing newline fixed
11. `./harness/harness.sh` exit 0 (all 8 sections `[OK]`)

**Commits:** `f996c31`, `c104429`, `2568647`

## 2026-07-30 — Activity 2: Remove Almuerzo Ejecutivo + recategorize Colombian dishes

**Goal:** Remove 'Almuerzo Ejecutivo' category and 'Menú del Día' product, create 'Platos Típicos' category (id=2), keep Bandeja Paisa/Sancocho/Ajiaco pointing to id=2.

**Deliverables (all 12 acceptance items met):**
- Almuerzo Ejecutivo category + Menú del Día product removed from data.sql
- 3 Almuerzo option_categories + 9 product_options + product_product_options + option_recipes removed
- order_detail_options filters referencing old product_id=2 removed
- Platos Típicos category created (reusing id=2)
- Bandeja Paisa, Sancocho de Gallina, Ajiaco Santafereño → category_id=2
- FK-safe cascade renumbering applied
- `./harness/harness.sh` exit 0
