# Plan 01 — Raw Material / Insumo Management (Foundation)

> Bounded context: `inventory` (existing). Adds **weighted average cost** discipline and manual adjustment flow on top of the current `supplies` / `supply_variants` / `inventory_stock` schema.

---

## 1. Scope

Maintain the master catalog of raw materials (insumos) used by the restaurant. Track current stock and **current weighted average unit cost** so other modules (purchasing, cost calculation, monthly closing) can rely on a single source of truth.

### In scope

- Insumo CRUD (`Supply`, `SupplyCategory`, `UnitOfMeasure`, `SupplyVariant`).
- Stock per storage location (`InventoryStock` — already exists).
- Weighted average unit cost (`SupplyVariant.unitCost` — column exists in V27, logic does not).
- Manual stock / cost adjustments (admin corrections, waste, transfers).

### Out of scope (handled by other plans)

- Weighted average **update on purchase** → Plan 02.
- Negative-stock enforcement from sales → already in `InventoryMovementService.deductForOrder`.
- Price suggestions for products → `SpecialSelectionPricingService` (existing).

---

## 2. Current state analysis

| Asset | Status | Notes |
|---|---|---|
| `supplies` | exists (V1) | `name UNIQUE`, FK to `supply_categories` |
| `supply_variants` | exists (V1 + V27) | `unit_cost DECIMAL(10,2)` already present, **never written** |
| `supply_categories` | exists (V1) | `name UNIQUE` |
| `units_of_measure` | exists (V1) | `name`, `abbreviation` UNIQUE |
| `inventory_stock` | exists (V1) | `(supply_variant_id, storage_location_id) UNIQUE` |
| `SupplyService` | missing | Only purchase-side services exist |
| `WeightedAverageUpdater` | missing | Must be a pure domain service |

---

## 3. Database schema (delta)

No schema changes required. `unit_cost` column already added in V27. A small enhancement migration is recommended to make the weighted average **explicit and auditable**:

```sql
-- V30__add_weighted_average_metadata.sql
ALTER TABLE supply_variants
    ADD COLUMN last_cost_updated_at TIMESTAMP NULL,
    ADD COLUMN last_purchase_item_id BIGINT NULL,
    ADD CONSTRAINT fk_sv_last_poi
        FOREIGN KEY (last_purchase_item_id) REFERENCES purchase_order_items(id) ON DELETE SET NULL;

CREATE INDEX idx_sv_last_cost_updated_at ON supply_variants(last_cost_updated_at);
```

Rationale:
- `last_cost_updated_at` → freshness indicator for the cost snapshot (used by monthly closing).
- `last_purchase_item_id` → forensic link to the purchase that caused the last change.

---

## 4. Domain layer

### 4.1 New pure helper (domain service)

```
core/inventory/domain/WeightedAverageCalculator.java
```

```java
public final class WeightedAverageCalculator {

  private WeightedAverageCalculator() {}

  /**
   * Returns the new weighted average price after a purchase is applied.
   *
   * @param currentStock   S_current (must be >= 0)
   * @param currentPrice   P_current (must be >= 0)
   * @param purchasedQty   Q_purchased (must be > 0)
   * @param purchasedPrice P_purchased (must be > 0)
   *
   * Formula: P_new = (S × P + Q × P') / (S + Q), rounded HALF_UP at scale 4 then 2.
   * Result is returned as a BigDecimal with scale = 2 (currency default).
   */
  public static BigDecimal compute(
      BigDecimal currentStock,
      BigDecimal currentPrice,
      BigDecimal purchasedQty,
      BigDecimal purchasedPrice) {

    Objects.requireNonNull(currentStock, "currentStock");
    Objects.requireNonNull(currentPrice, "currentPrice");
    Objects.requireNonNull(purchasedQty, "purchasedQty");
    Objects.requireNonNull(purchasedPrice, "purchasedPrice");

    if (currentStock.signum() < 0 || currentPrice.signum() < 0
        || purchasedQty.signum() <= 0 || purchasedPrice.signum() <= 0) {
      throw new InvalidWeightedAverageInputException(...);
    }

    // Intermediate scale 4 to absorb rounding drift; final scale 2 for storage.
    BigDecimal numerator   = currentStock.multiply(currentPrice)
                                  .add(purchasedQty.multiply(purchasedPrice));
    BigDecimal denominator = currentStock.add(purchasedQty);

    if (denominator.signum() == 0) {
      // currentStock == 0 && purchasedQty == 0  →  guarded above, defensive return
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return numerator.divide(denominator, 4, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
  }
}
```

> **BigDecimal discipline (project-wide)** — see Plan 06 for the full `Money` value-object refactor. Until Plan 06 lands, every monetary field uses this helper's contract: scale 2, `HALF_UP`. Never use `double` or `float` for money. All division explicitly passes scale + rounding mode (no default `BigDecimal.divide(BigDecimal)` allowed).

### 4.2 New exceptions (`application/exception/`)

- `InvalidWeightedAverageInputException` (→ 400)
- `SupplyNotFoundException` already exists
- `SupplyVariantNotFoundException` already exists
- `InsufficientStockForAdjustmentException` (when manual adjustment tries to drive stock < 0) → 409

---

## 5. Application layer (use cases)

Each use case is a `@Service` in `application/inventory/service/` implementing a port in `domain/inventory/port/input/`.

| Use case | Port verb | Notes | Audit |
|---|---|---|---|
| `CreateSupplyService` | `create(...)` | Throws `SupplyAlreadyExistsException` if `name` taken | yes — `SUPPLY_CREATED` |
| `UpdateSupplyService` | `update(...)` | Rejects category change if variants exist | yes — `SUPPLY_UPDATED` |
| `DeactivateSupplyService` | `deactivate(...)` | Soft delete; variants remain queryable | yes — `SUPPLY_DEACTIVATED` |
| `CreateSupplyVariantService` | `create(...)` | Composite unique `(supply_id, unit_id, quantity)` enforced | yes — `VARIANT_CREATED` |
| `UpdateSupplyVariantService` | `update(...)` | Allows editing `unit_cost` **only** via `AdjustWeightedAverageUseCase` | yes — `VARIANT_UPDATED` |
| `AdjustWeightedAverageUseCase` | `adjust(supplyVariantId, newPrice, reason, performedBy)` | **Manual** admin override (see § 6.2) | **yes — `WEIGHTED_AVG_ADJUSTED` with old/new price + reason** |
| `AdjustStockUseCase` | `adjust(variantId, locationId, delta, reason, performedBy)` | Manual stock correction; writes `MovementType.ADJUSTMENT` | **yes — `STOCK_ADJUSTED` with old/new qty + reason** |
| `CreateSupplyCategoryService` | `create(...)` | name UNIQUE | yes — `CATEGORY_CREATED` |
| `CreateUnitOfMeasureService` | `create(...)` | name + abbreviation UNIQUE | yes — `UOM_CREATED` |

**Audit contract** — every use case that mutates money or stock MUST append an `audit_logs` row (existing `core/common/audit/domain/AuditLog.java`). The audit payload includes:

- `action` (enum from `AuditAction`)
- `entityType` (e.g., `SUPPLY_VARIANT`)
- `entityId`
- `performedBy` (user id; null only for system actions like the cron)
- `performedAt` (UTC `Instant`, not `LocalDateTime` — server-local TZ is wrong for audit)
- `previousState` (JSON snapshot of relevant fields BEFORE the change)
- `newState` (JSON snapshot of relevant fields AFTER)
- `reason` (free text from admin; required for manual adjustments, optional for system-driven changes)

> Convention: every use case method takes a **command** DTO and returns the domain entity. Logging via the project's `Logger` port. No `@Transactional` here — the wrapping infrastructure service owns transaction boundaries (see architecture.md).

---

## 6. Core business logic

### 6.1 Weighted average (informational)

```
P_new = (S_current × P_current + Q_purchased × P_purchased) / (S_current + Q_purchased)
```

- `P_new` stored in `supply_variants.unit_cost`.
- `last_cost_updated_at = NOW()`.
- All values `DECIMAL(10,2)` except the intermediate division keeps **4 decimals** to reduce rounding drift before rounding to 2.
- **Denominator == 0** is impossible in practice (`Q_purchased > 0` enforced), but the helper guards it defensively.

### 6.2 Manual adjustment flow (`AdjustWeightedAverageUseCase`)

Reason: admin override when market prices shift without a purchase (e.g., supplier renegotiation).

```
AdjustWeightedAverageCommand(variantId, newPrice, reason, performedBy)
  ├─ validate newPrice > 0
  ├─ lock variant row (PESSIMISTIC_WRITE) + @Version check
  ├─ capture oldPrice BEFORE the change
  ├─ write supply_variants.unit_cost = newPrice
  ├─ write supply_variants.last_cost_updated_at = Instant.now()  (UTC)
  ├─ append audit log:
  │     action:       ADJUST_WEIGHTED_AVG
  │     entityType:   SUPPLY_VARIANT
  │     entityId:     variantId
  │     performedBy:  performedBy
  │     performedAt:  Instant.now()         -- UTC, NOT LocalDateTime
  │     previousState: { unitCost: oldPrice.toPlainString() }
  │     newState:      { unitCost: newPrice.toPlainString() }
  │     reason:       reason                -- required; reject if blank
  └─ return updated SupplyVariant
```

`adjustStock` does **not** change `unit_cost` — it only updates `inventory_stock.current_quantity` and writes an `ADJUSTMENT` movement (extend `MovementType` enum).

### 6.3 Stock integrity

- **Strict invariant**: `inventory_stock.current_quantity >= 0` for the default `Bodega` location.
- Enforced by:
  - DB `CHECK (current_quantity >= 0)` — **add** in a new migration (see § 3 enhancement).
  - Service-level guard inside `AdjustStockUseCase` and `InventoryMovementService`.

```sql
-- part of V30
ALTER TABLE inventory_stock
    ADD CONSTRAINT chk_stock_nonneg CHECK (current_quantity >= 0);
```

---

## 7. Infrastructure layer

### 7.1 Persistence

No new entities — reuse `SupplyVariantEntity`, `InventoryStockEntity`, `SupplyEntity`, `SupplyCategoryEntity`, `UnitOfMeasureEntity`.

**Add `@Version` column** to `SupplyVariantEntity` so Hibernate increments it on every update; this catches any race that slips past the pessimistic lock (e.g., a connection-pool timeout that dropped the lock silently).

```sql
-- V30_1__add_supply_variant_version.sql
ALTER TABLE supply_variants
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

```java
@Entity
@Table(name = "supply_variants", uniqueConstraints = ...)
public class SupplyVariantEntity {
  // ... existing fields ...

  @Version
  @Column(name = "version", nullable = false)
  private Long version;     // ← added in V30_1
}
```

**Lock methods** on ports for concurrency-safe weighted average updates:

```java
// SupplyVariantRepositoryPort (added)
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints({
    @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
})
Optional<SupplyVariant> findByIdForUpdate(Long id);
```

Adapter delegates to a `@Query` method on `SupplyVariantRepository` annotated with `@Lock`. The 3-second timeout hint ensures the request fails fast (Hibernate will throw `PessimisticLockException` translated by Spring as `PessimisticLockingFailureException`) instead of stalling the HTTP thread.

**Retry on optimistic-lock failures** (caller side):

```java
@Retryable(
    retryFor = { ObjectOptimisticLockingFailureException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 200, multiplier = 2.0)
)
public Money updateWeightedAverage(...) { ... }
```

### 7.2 REST API

Base path: `/api/v1/inventory`. All controllers carry `@Tag(name = "Inventory")`.

| Method | Path | Purpose | Status codes |
|---|---|---|---|
| `POST` | `/supply-categories` | Create category | 201 / 409 |
| `GET`  | `/supply-categories` | List categories | 200 |
| `POST` | `/units-of-measure` | Create UoM | 201 / 409 |
| `GET`  | `/units-of-measure` | List UoM | 200 |
| `POST` | `/supplies` | Create supply | 201 / 409 |
| `GET`  | `/supplies` | List supplies (paginated, filter by category) | 200 |
| `GET`  | `/supplies/{id}` | Get supply | 200 / 404 |
| `PATCH`| `/supplies/{id}` | Update supply | 200 / 404 |
| `DELETE`| `/supplies/{id}` | Soft delete | 204 / 404 |
| `POST` | `/supplies/{id}/variants` | Create variant | 201 / 409 |
| `GET`  | `/supplies/{id}/variants` | List variants | 200 |
| `PATCH`| `/supply-variants/{id}/weighted-average` | Manual cost override | 200 / 400 / 404 |
| `POST` | `/supply-variants/{id}/stock-adjustments` | Manual stock adjust | 200 / 409 |
| `GET`  | `/supply-variants/{id}` | Get variant with current stock + cost | 200 / 404 |

#### Request / response shapes (representative)

```json
// POST /supplies
{
  "name": "Tomato",
  "supplyCategoryId": 3
}

// 201 Created
{
  "id": 17,
  "name": "Tomato",
  "supplyCategoryId": 3,
  "active": true
}

// PATCH /supply-variants/42/weighted-average
{
  "newUnitCost": 1.25,
  "reason": "Supplier renegotiation"
}

// 200 OK
{
  "id": 42,
  "supplyId": 17,
  "unitId": 2,
  "quantity": 1.000,
  "unitCost": 1.25,
  "lastCostUpdatedAt": "2026-07-14T10:00:00Z",
  "lastPurchaseItemId": null
}
```

Every endpoint carries `@Operation(summary, description)` and `@ApiResponse` per architecture.md §Swagger / OpenAPI.

---

## 8. Concurrency model

| Operation | Lock | Version | Isolation |
|---|---|---|---|
| Weighted average recompute on purchase | `PESSIMISTIC_WRITE` + 3s timeout on `supply_variants(id)` | `@Version` | `READ_COMMITTED` (default) |
| Manual cost adjustment | `PESSIMISTIC_WRITE` + 3s timeout on `supply_variants(id)` | `@Version` | `READ_COMMITTED` |
| Stock addition (Bodega entry) | `PESSIMISTIC_WRITE` + 3s timeout on `inventory_stock(variant, location)` | — | `READ_COMMITTED` |
| Stock deduction (sale) | Same row lock + ordered by location | — | `READ_COMMITTED` (existing) |
| Stock adjustment (manual) | `PESSIMISTIC_WRITE` + 3s timeout on `inventory_stock(variant, location)` | — | `READ_COMMITTED` |

Two simultaneous purchases for the same variant serialize on the variant row → weighted average math is always computed against a consistent `(S_current, P_current)` snapshot. The `@Version` column catches any race that slips past the pessimistic lock (connection-pool timeout, lock dropped, etc.) and is translated by Spring's `ObjectOptimisticLockingFailureException`. The caller retries via `@Retryable` (3 attempts, exponential backoff). This addresses the user-stated constraint: *"the database transactions must be isolated to prevent incorrect stock/cost calculations."*

---

## 9. Tests

| Test class | Coverage |
|---|---|
| `WeightedAverageCalculatorTest` | Pure unit — happy path, zero stock, equal prices, max precision |
| `AdjustWeightedAverageServiceTest` | Locks row, writes audit, rejects negative price |
| `AdjustStockServiceTest` | Rejects delta that would drive stock < 0; writes ADJUSTMENT movement |
| `CreateSupplyVariantServiceTest` | Enforces `(supply, unit, qty)` UNIQUE |
| `InventoryStockIntegrationTest` | Concurrent purchase + manual adjust → final stock & cost correct |

---

## 10. Acceptance criteria

- [ ] `V30` migration applied (adds `last_cost_updated_at`, `last_purchase_item_id`, `CHECK` constraint, index).
- [ ] `V30_1` migration adds `version BIGINT` column to `supply_variants`.
- [ ] `SupplyVariantEntity` carries `@Version` and `findByIdForUpdate` has the 3-second timeout hint.
- [ ] `WeightedAverageCalculator` is framework-free and unit-tested; uses `BigDecimal` exclusively.
- [ ] `AdjustWeightedAverageUseCase` writes audit entry and updates `last_cost_updated_at`.
- [ ] All REST endpoints listed in § 7.2 implemented with `@Tag` / `@Operation` / `@ApiResponse`.
- [ ] Concurrency: simulated parallel purchases do not corrupt `unit_cost`.
- [ ] `@Retryable` on `ObjectOptimisticLockingFailureException` exercised by a concurrency IT.
- [ ] `./harness/harness.sh` exits 0.

---

## 11. Risks / open questions

- **Decimal scale**: V27 uses `DECIMAL(10,2)` — sufficient for unit cost but intermediate math uses `DECIMAL(19,4)` to avoid cumulative drift. Document in code.
- **Manual adjustments bypassing the formula**: keep audit log mandatory; reject empty `reason`.
- **Soft delete of supply with active variants**: blocked at service level — documented, not enforced by DB.