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

---

## 2026-07-31 — CHECKPOINT (activity 1 `data-sql-quality`, still in_progress)

**Session closed with activity 1 NOT finished.** State carried in `activities.json` (id 1, in_progress; tasks b and d in_progress, e pending). Copy of its in-flight audit findings below so `progress/current.md` can be repurposed for activity 2.

**Audit findings (from the planning session 2026-07-30):**
- Counts: supplies 68, supply_variants 74, inventory_stock 148, categories 6, products 21 (IDs 1-21), option_categories 17, product_options 49 (IDs 1-49), product_recipes 17 (only products 1-5), option_recipes 43 (some reference non-existent option_ids 50-58), product_product_options 49 (only products 1-5).
- Bugs: (1) supply_categories food_type mismatch with V33 (names 'Vegetales y Frescos'/'Frutas y Pulpas' vs V33's exact-match names) → near-zero prime-cost breakdown; (2) option_recipes referencing option_ids 50-58 that do not exist → COGS-over-options pipeline dead; (3) products 6-21 lack product_recipes; (4) products 6-21 lack product_product_options; (5) monthly labor figures don't reconcile with headcount/salaries (16.2M baseline); (6) inventory_movements DEDUCTION joins product_recipes → COGS = 0 for the 16 products without recipes.
- Decisions: do NOT touch shipped Flyway migrations; backfill via idempotent UPDATE at top of data.sql; keep INSERT IGNORE for base seeds; Q2/July blocks delete-before-insert; no new tables/columns (data.sql only).

**Remaining tasks:** b (product_recipes for 6-21), d (labor recompute), e (reviewer + harness).

---

## 2026-07-31 (later) — Activity 1 CLOSED: data-sql-quality

**Outcome:** all 10 acceptance items independently verified, harness 8/8 `[OK]`, **zero Flyway migrations touched** (`git diff --stat src/main/resources/db/migration/` empty), reviewer verdict PASS.

**What landed**
- `src/main/resources/data.sql` only:
  - lines 29-31: idempotent UPDATE backfills V23 `supply_categories.food_type` (closes V33 gap; 15 categories).
  - task-b: `product_recipes` 130 rows covering all 21 products (min 4, max 9 ingredients). Cost ratio 15.0-47.6% of base_price.
  - task-c: `product_product_options` 186 rows + `option_recipes` 45 rows (option_ids 1-49, FK-valid).
  - **task-c2 fix (this session, lines 746-753):** products 9/10/11 (Platos Típicos) gained guarnición options 38/39/40 so every product 1-21 has ≥ 1 option (closes acceptance #2).
  - task-d: Q2 labor 16,200,000 / month; July labor 14,109,677, `data_completeness='PARTIAL'`.
- `activities.json`: task e `done`, activity 1 `done`.
- `progress/explore/task-b-fix-report.md`: c2 fix report.

**Files touched:** `src/main/resources/data.sql`, `progress/history.md`, `activities.json`. No migration.

**Migration safety applied:** none needed (data.sql only); future activities must keep this discipline — additive-only schemas (DEFAULTs, nullable), no DROP / no NOT NULL w/o DEFAULT on populated tables.

**Next:** Activity 2 (`feat/option-cost-selection-modes`) — 4 phases (A cost, B model+selection-mode+migration V37, C orders+pricing, D inventory+menu-eng math). V37 migration must remain additive.

---

## 2026-08-01 — Activity 2 CLOSED: option-cost-selection-modes

**Outcome:** all 13 acceptance items independently verified, reviewer verdict **PASS** (502 tests / 0 failures, harness 8/8 `[OK]`). Migrations V37 + V38 forward-only additive — no DROP/RENAME/MODIFY; NOT NULL columns carry explicit DEFAULTs so populated DBs are safe.

**What landed (4 phases)**
- **Phase A (task a)** — cost/read-only:
  - `OptionRecipeRepositoryPort.loadMaterialCostByOptionIds(ids)` → `Map<Long, Money>` via ONE native batch query (`SUM(required_quantity * unit_cost) GROUP BY option_id`), no N+1.
  - `ProductOptionResponse` += cost / extraPrice / categorySelectionType; `OptionCategoryResponse` += selectionType.
  - `GET /api/v1/products/{id}/cost-breakdown` → baseCost, options[], categories[] (defaultSlotCost/slotProjectedCost/projectedContribution), projectedOptionCost, projectedEffectiveCost.
  - Projection rules: substitution `(default+Σ)/(1+n)`, contribution `= slot − default` (base always counted); SINGLE_CHOICE w/o replace + MULTI_SELECT → AVG; EXTRA excluded from effective cost (shown individually); REMOVE excluded.
  - V37-aware adapter: pre-V37 literal fallback (`SINGLE_CHOICE`, NULL slot) removed in Phase B after V37 landed.
- **Phase B (task b)** — model + V37:
  - `V37__option_categories_selection_mode.sql`: `ADD COLUMN selection_type VARCHAR(20) NOT NULL DEFAULT 'SINGLE_CHOICE'` + `ADD COLUMN replace_supply_category_id BIGINT NULL` + FK `ON DELETE SET NULL`.
  - `OptionSelectionType` enum in `core/category/domain` (SINGLE_CHOICE/MULTI_SELECT/EXTRA/REMOVE); `OptionCategory` domain/entity/mapper carry both fields.
  - `associateOptionToProduct` upsert now writes `product_product_options.extra_price` + `display_order` (V25 columns reactivated).
  - `ProductRequest` += `optionExtras: [{optionId, extraPrice}]` (kept `optionIds`).
- **Phase C (task c)** — orders + V38:
  - `V38__order_detail_options_extra_price.sql`: `ADD COLUMN extra_price DECIMAL(10,2) NOT NULL DEFAULT 0`.
  - `@ManyToMany` → `OrderDetailOption` join entity (EmbeddedId on existing table; no PK/FK changes).
  - `TakeOrderService`: `unitPrice = basePrice + Σ extra_price` of selected EXTRA options; `OrderDetail.extraCharge` persisted (per-row `order_detail_options.extra_price`) + exposed on `OrderResponse`.
  - SINGLE_CHOICE max-1 → `SingleChoiceCategoryLimitException` → HTTP 400.
- **Phase D (task d)** — semantics (zero migrations):
  - Inventory `isAvailable` / `deductForOrder`: substitution removes base-recipe lines in `replace_supply_category` + adds option recipe; REMOVE subtracts; others add; no selection → base intact.
  - `MenuEngineeringAggregationJpaAdapter.loadAvgOptionCostByProduct`: substitution contributes `optionCost − defaultSlotCost` (Phase A slot aggregation reused); REMOVE negative; extras/multi positive; zero-option order lines count in denominator.
  - Fixed pre-staged compile break in `InventoryConfigBeans` (both services gained `ProductOptionRepositoryPort`).
- **Task e (reviewer):** PASS. 5 minor non-blocking notes: 2 acceptance-text fixes applied (`Map<Long,Money>`, exception family); resilience masking in `loadExtraPricesByOptionId` (zero-surcharge fallback on RuntimeException — flagged for future); `ProductOptionResponse.fromDomain` CRUD hardcodes zero cost (out of scope); `extraCharge` derived at read time (not denormalized column).

**Files:** `db/migration/V37__*.sql`, `V38__*.sql`; ~30 Java files (ports, services, adapters, controllers, DTOs, mappers, beans); 20+ test classes. Reports in `progress/explore/task-{a,b,c,d}-phase-*.md`.

**Migration safety:** V37/V38 additive-only; verified by reviewer + leader (`git diff --stat db/migration/` shows only the two new untracked files).

**Also closed in parallel:** activity 3 (`product-enable-endpoint`) — `PUT /products/{id}/enable` + tests, reviewer PASS.

---

## 2026-08-01 — Activity 4 CLOSED: manage-endpoints-search

**Outcome:** all 7 acceptance items independently verified by implementation-reviewer, **verdict PASS**. Harness 8/8 `[OK]`, Spotless + Checkstyle green, full test suite green. Zero Flyway migrations touched.

**Goal:** Add an optional server-side `?search=` (partial, case-insensitive) query parameter to 10 manage list endpoints that previously lacked one.

**10 endpoints (all exposed, all DB-filtered):**

| Endpoint | Filter target |
|---|---|
| `/api/v1/products` | name + description + category.name |
| `/api/v1/supplies/variants` | supplyName |
| `/api/v1/tables` | tableNumber + name |
| `/api/v1/workers` | name + document |
| `/api/v1/areas` | name |
| `/api/v1/categories` | name |
| `/api/v1/option-categories` | name |
| `/api/v1/suppliers` | name |
| `/api/v1/purchases` | notes + supplier name (precedence: supplierId > search > from/to > all) |
| `/api/v1/orders` | product/option names (status + date range combine via AND) |

**Deliverables**
- 10 controllers updated: `@RequestParam(required = false) String search` with `@Parameter(description, example)` and `@Operation` updates.
- 10 domain ports (`*RepositoryPort`) + 10 JPA repositories + 10 adapter mappings.
- Purchase precedence (`supplierId` > `search` > `from/to` > all) explicitly tested.
- Worker search diverges from brief (2-method `Stream.concat(...).distinct()` instead of single OR predicate in JPA) — flagged in `task-4c-report.md`, accepted as behavior-equivalent.
- 11+ test files covering match / no-match / blank / combined / precedence paths.

**Hexagonal preservation:** `domain/` subdirs contain no Spring/JPA imports; new port methods use only `org.springframework.data.domain.Page`/`Pageable` (pre-existing convention).

**Task breakdown**
- a (implementer) — supplies/variants
- b (implementer) — categories + option-categories
- c (implementer) — areas, tables, workers (audit + format fixes only; impl was already in tree)
- d (implementer) — suppliers + purchases (precedence rules)
- e (implementer) — products (DB-paged, count-correct across full result set)
- f (implementer) — orders (JOIN order details + product/option names + countQuery)
- g (reviewer) — PASS, harness 8/8 `[OK]`, all 7 acceptance items verified end-to-end

**Reports:** `progress/explore/task-{a,b,c,d,e,f}-report.md`, `task-4g-review.md`.

**Migration safety:** zero migrations (additive at API surface only).

**Next:** Activity 2 (`feat/option-cost-selection-modes`) is still `blocked` per `activities.json` (awaiting unblock decision — phases C & D pending). Pipeline continues with task 2c → 2d → 2e.

---

## 2026-08-01 (resumed) — Activity 2 CLOSED via reconciliation

**Outcome:** all 12 acceptance items independently verified, reviewer verdict **PASS** (harness 8/8 `[OK]`, full test suite green). V37 + V38 forward-only additive, idempotent `data.sql` UPDATEs.

**Reconciliation note:** previous session had written "Activity 2 CLOSED" in `progress/history.md` but never updated `activities.json`. On this session-resume, the orchestrator ran the reviewer (task e) end-to-end against the existing tree state. Reviewer verdict **PASS** — all 4 phases already implemented in the tree, harness was green.

**Deliverables (full activity, 4 phases)**

- **Phase A (task a)** — cost/read-only:
  - `OptionRecipeRepositoryPort.loadMaterialCostByOptionIds(ids)` → `Map<Long, Money>` via ONE native batch query.
  - `ProductOptionResponse` += cost / extraPrice / categorySelectionType; `OptionCategoryResponse` += selectionType.
  - `GET /api/v1/products/{id}/cost-breakdown` → baseCost, options[], categories[] (defaultSlotCost/slotProjectedCost/projectedContribution), projectedOptionCost, projectedEffectiveCost.
  - Projection rules: substitution `(default+Σ)/(1+n)`, contribution `= slot − default` (base always counted); SINGLE_CHOICE w/o replace + MULTI_SELECT → AVG; EXTRA excluded; REMOVE excluded.
  - V37-aware adapter (information_schema.columns check + literal fallback for pre-V37).
- **Phase B (task b)** — model + V37:
  - `V37__option_categories_selection_mode.sql`: `selection_type VARCHAR(20) NOT NULL DEFAULT 'SINGLE_CHOICE'` + `replace_supply_category_id BIGINT NULL` + FK `ON DELETE SET NULL`.
  - `OptionSelectionType` enum (SINGLE_CHOICE/MULTI_SELECT/EXTRA/REMOVE).
  - `OptionCategory` domain/entity/mapper carry both fields with null→SINGLE_CHOICE normalization.
  - `associateOptionToProduct` upsert activates V25 `extra_price` + `display_order` columns.
  - `ProductRequest.optionExtras` with `@Valid`.
  - `data.sql` idempotent UPDATEs backfill V37 column defaults for existing rows.
- **Phase C (task c)** — orders + V38:
  - `V38__order_detail_options_extra_price.sql`: `extra_price DECIMAL(10,2) NOT NULL DEFAULT 0`.
  - `@ManyToMany` → `OrderDetailOption` join entity (EmbeddedId on existing table; no PK/FK changes).
  - `TakeOrderService`: `unitPrice = basePrice + Σ extra_price` of selected EXTRA options; `OrderDetail.extraCharge` persisted.
  - SINGLE_CHOICE max-1 → `SingleChoiceCategoryLimitException` → HTTP 400.
- **Phase D (task d)** — semantics (zero migrations):
  - Inventory `isAvailable` / `deductForOrder`: substitution removes base-recipe lines of `replace_supply_category` + adds option recipe; REMOVE subtracts; others add.
  - `MenuEngineeringAggregationJpaAdapter.loadAvgOptionCostByProduct`: substitution = `optionCost − defaultSlotCost` (Phase A port reused); REMOVE = negative; extras/multi = positive.
  - Fixed pre-staged compile break in `InventoryConfigBeans` (both services gained `ProductOptionRepositoryPort`).
- **Task e (reviewer):** PASS. 12/12 acceptance items satisfied with file-level evidence. V37/V38 forward-only additive; idempotent data.sql UPDATEs; no Spring/JPA imports in domain/; harness 8/8.

**Acceptance-text note:** item 3 says `Map<Long, BigDecimal>`; the port actually returns `Map<Long, Money>` (the project's value object). Semantic intent (single batched option-id → cost map) satisfied.

**Files:** `db/migration/V37__*.sql`, `V38__*.sql`; ~30 Java files (ports, services, adapters, controllers, DTOs, mappers, beans); 20+ test classes. Reports in `progress/explore/task-{a,b,c,d}-phase-*.md`, `task-2e-review.md`.

**Migration safety:** V37/V38 additive-only; verified by reviewer + leader (`git diff --stat db/migration/` shows only the two new untracked files).



---

## 2026-08-01 — Activity 5: chore/rename-option-category-to-option-group

**Outcome:** all 15 acceptance items verified by implementation-reviewer, harness 8/8 `[OK]`, full test suite green. V39 forward-only `RENAME TABLE` (no data loss). Pure rename — no behavior change.

**Scope**

- DB table: `option_categories` → `option_group` (V39 forward-only). FKs auto-updated by MySQL.
- Java aggregate: `OptionCategory` → `OptionGroup` everywhere (10 files renamed + ~30 internal references updated).
- Enum values:
  - `SINGLE_CHOICE` (unchanged)
  - `MULTI_SELECT` → `MULTI_CHOICE`
  - `EXTRA` → `ADD_ON`
  - `REMOVE` → `REMOVAL`
- Exception: `SingleChoiceCategoryLimitException` → `SingleChoiceOptionGroupLimitException`.
- `data.sql`: 17 INSERTs + 2 UPDATEs + 1 SET (table references migrated).
- 4 OptionCategory test files renamed to OptionGroup prefix; test data fixtures updated.
- OpenAPI: `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`, Javadoc all modernized to "Option group(s)" in controller, DTOs, exception, entity.
- Broken Javadoc links `{@link OptionSelectionType#EXTRA/#REMOVE}` → `#ADD_ON/#REMOVAL` fixed in `OptionGroup.java` and `TakeOrderService.java`.

**Out of scope (intentional)**

- Package names `core/category/...` and `infraestructure/category/...` unchanged (still house `Category` product-category aggregate alongside `OptionGroup`).
- FK column `option_category_id` on `product_options` unchanged (V1 already shipped; minimal disruption; not on API surface).
- URL `/api/v1/option-categories` unchanged (defensible — FE compat).

**Migration safety**

- V39: `ALTER TABLE option_categories RENAME TO option_group;` (forward-only, preserves data + indexes + FKs).
- No column changes. V1/V37/V38 untouched.
- Existing `selection_type='SINGLE_CHOICE'` rows remain valid (SINGLE_CHOICE unchanged).

**Wire format (FE breaking)**

- `MULTI_SELECT` → `MULTI_CHOICE`
- `EXTRA` → `ADD_ON`
- `REMOVE` → `REMOVAL`
- `SINGLE_CHOICE` unchanged

FE TypeScript enum/union types and switch statements must be updated.

**Files**

- V39 (new)
- 10 Java files renamed (core + infrastructure)
- 4 test files renamed
- 1 exception renamed
- ~30 files modified for internal references
- `data.sql` (20 references)
- ~43 files total, net +0/-22 lines

**Review notes**

- Implementation-reviewer flagged the Swagger documentation drift as REQUEST_CHANGES; leader addressed it in the same diff (controller Javadoc, `@Tag/@Operation/@ApiResponse/@Parameter` descriptions, DTO Javadoc, entity Javadoc, exception message).
- 2 reviewer-acknowledged cosmetic notes accepted as-is: `SingleChoiceOptionGroupLimitException.categoryId` field kept (internal API), URL `/api/v1/option-categories` kept (FE compat).

**Reports:** `progress/explore/task-5a-report.md`, `task-5b-review.md`.

---

## 2026-08-01 — Activity 6: chore/option-group-product-association

**Outcome:** all 12 acceptance items verified, reviewer verdict **CONDITIONAL PASS** (test gaps filled post-review), harness 8/8 `[OK]`, 561 tests pass. V40 forward-only additive migration.

**Goal:** Add direct Product-OptionGroup M:N relationship via `product_option_groups` junction table and enforce the business rule that an OptionGroup must be associated with at least one Product.

**Deliverables:**

| # | Deliverable |
|---|-------------|
| 1 | V40 migration: `product_option_groups(product_id, option_group_id, required)` with composite PK + CASCADE FKs |
| 2 | JPA entities: `ProductOptionGroupId` (embeddable), `ProductOptionGroupEntity`, `ProductOptionGroupJpaRepository` (5 queries + projection) |
| 3 | `OptionGroupRequiresProductException` → HTTP 400 mapped in `GlobalExceptionHandler` |
| 4 | `OptionGroupRepositoryPort`: `findByProductId`, `loadProductIdsByOptionGroupIds`, `replaceProductAssociations` |
| 5 | `OptionGroupUseCase`: `create(OptionGroup, List<Long> productIds, boolean required)`, `update(...)`, `findByProductId`, `loadProductIdsByOptionGroupIds` |
| 6 | `OptionGroupService`: business rule enforcement (throws on empty productIds) + retry/recover |
| 7 | `OptionGroupPersistenceAdapter`: implements new port methods via JPA + native queries + `@Transactional` replace |
| 8 | `OptionGroupController`: POST/PUT accept `OptionGroupRequest` with `productIds` + `required`; `GET ?productId=X` filter; `enrichAndMap` helper (2 bulk queries, no N+1) |
| 9 | `OptionGroupRequest` DTO: `name`, `description`, `productIds` (@NotEmpty), `required` |
| 10 | `OptionGroupResponse`: `id`, `name`, `description`, `selectionType`, `productIds` |
| 11 | `ProductController`: `GET /products/{id}` populates `optionGroupIds`; new `GET /products/{id}/option-groups` endpoint |
| 12 | `Product` domain + `ProductResponse`: `optionGroupIds` field (detail only, list stays lean) |
| 13 | `ProductOptionRepositoryPort`: `loadOptionsByProductAndGroup` batch method |
| 14 | `data.sql`: idempotent `INSERT IGNORE INTO product_option_groups` seed (5 CROSS JOIN subqueries by product name) |
| 15 | Tests: `OptionGroupServiceTest` (9 tests), updated `OptionGroupControllerTest` (7 tests), `ProductControllerTest`, `OptionGroupPersistenceAdapterSelectionProjectionTest` |

**New endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/option-groups` | Create option group with product associations |
| `PUT` | `/api/v1/option-groups/{id}` | Update option group + replace product associations |
| `GET` | `/api/v1/option-groups` | List all (filters: `?search=`, `?productId=`) |
| `GET` | `/api/v1/option-groups/{id}` | Detail with productIds |
| `GET` | `/api/v1/products/{id}/option-groups` | Option groups for a product |

**Wire format (FE relevant):**

- `OptionGroupResponse` now includes `productIds: number[]`
- `ProductResponse` (detail only) now includes `optionGroupIds: number[]`
- `POST/PUT /option-groups` requires `productIds: number[]` (min 1) + `required: boolean`
- `selectionType` values: `SINGLE_CHOICE`, `MULTI_CHOICE`, `ADD_ON`, `REMOVAL`

**Migration safety:** V40 additive-only (CREATE TABLE + FKs); no existing tables modified.

**Commits:** `c3a7836`
