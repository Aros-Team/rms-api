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
| b (absorbed into a) | MoneyCalculator already built in task a |
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