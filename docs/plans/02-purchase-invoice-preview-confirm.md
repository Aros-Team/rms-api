# Plan 02 — Purchase Invoice Module (Bulk Upload, Preview, Confirm)

> Bounded context: `purchase` (existing) + light extension of `inventory` (Plan 01). Replaces the current one-step `RegisterPurchaseOrderService` with a two-step **Preview → Confirm** transactional flow.

---

## 1. Scope

Process a **validated, structured JSON invoice payload** delivered by the frontend (which itself has already consumed the OCR / parsing service). The backend:

1. Receives the payload.
2. Matches each line item against `supplies` / `supply_variants`.
3. Auto-creates missing insumos on the fly (per item flag).
4. Returns a **Preview** describing old vs new stock and weighted average.
5. On explicit confirm, performs all DB mutations inside a single SQL transaction (atomic).

### Out of scope

- PDF / XML / image parsing (frontend responsibility per user constraints).
- Supplier scoring, multi-currency, taxes.
- Returns / credit notes (separate future module).

---

## 2. Current state analysis

| Asset | Status | Gap |
|---|---|---|
| `suppliers` | exists | OK |
| `purchase_orders` | exists | OK |
| `purchase_order_items` | exists (V2) | OK |
| `RegisterPurchaseOrderService` | exists | One-step; **no preview**; **no auto-create insumo**; **does not update `unit_cost`** |
| `PurchaseOrderItem.unit_price` | exists | OK |

**Critical gap**: `RegisterPurchaseOrderService` does not invoke `WeightedAverageCalculator` (Plan 01). This plan adds that and the preview/confirm split.

---

## 3. Database schema (delta)

Two new tables and one column addition.

### 3.1 `purchase_sessions` (preview/confirm state machine)

```sql
-- V31__create_purchase_sessions.sql
CREATE TABLE purchase_sessions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_name     VARCHAR(255) NOT NULL,
    invoice_number    VARCHAR(120),
    purchased_at      TIMESTAMP    NOT NULL,
    registered_by     BIGINT       NOT NULL,
    payload_json      JSON         NOT NULL,        -- frozen copy of the request
    preview_json      JSON         NOT NULL,        -- computed snapshot
    status            VARCHAR(20)  NOT NULL,        -- PREVIEWED | CONFIRMED | CANCELLED | EXPIRED
    purchase_order_id BIGINT       NULL,            -- set on CONFIRM
    confirmation_idempotency_key VARCHAR(120) NULL, -- set on confirm; UNIQUE with status=CONFIRMED
    version           BIGINT       NOT NULL DEFAULT 0,    -- @Version for optimistic lock
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at      TIMESTAMP    NULL,
    expires_at        TIMESTAMP    NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL 24 HOUR),
    CONSTRAINT fk_ps_user           FOREIGN KEY (registered_by)      REFERENCES users(id),
    CONSTRAINT fk_ps_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE SET NULL,
    CONSTRAINT chk_ps_status        CHECK (status IN ('PREVIEWED','CONFIRMED','CANCELLED','EXPIRED'))
);

CREATE INDEX idx_ps_status_expires ON purchase_sessions(status, expires_at);
-- NB: idempotency uniqueness is enforced by a partial unique index implemented
-- as a separate index on (idempotency_key) filtered at the application layer.
-- MySQL does not support partial unique indexes; we rely on:
--   (a) the application-level `findByIdempotencyKeyAndStatus(idempotencyKey, CONFIRMED)` lookup
--       BEFORE writing, and
--   (b) a regular UNIQUE index on (idempotency_key, status) — see V31_1.
```

**Idempotency rationale**: MySQL allows multiple NULL values in a UNIQUE index. We can safely store sessions without a key. For sessions that DO carry an idempotency key, uniqueness is enforced **only when status flips to CONFIRMED** — hence the compound index `(idempotency_key, status)` added below.

```sql
-- V31_1__add_idempotency_unique_index.sql
CREATE UNIQUE INDEX uq_ps_idemp_confirmed
    ON purchase_sessions(confirmation_idempotency_key, status);
-- Multiple NULL keys allowed (preview sessions); collisions caught at confirm time.
```

### 3.2 `purchase_session_items` (resolved line items)

```sql
CREATE TABLE purchase_session_items (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id            BIGINT         NOT NULL,
    raw_insumo_name       VARCHAR(255)   NOT NULL,
    raw_unit              VARCHAR(20)    NOT NULL,
    raw_quantity          DECIMAL(10,3)  NOT NULL,
    raw_unit_price        DECIMAL(10,2)  NOT NULL,
    resolved_supply_id    BIGINT         NULL,            -- matched or auto-created
    resolved_variant_id   BIGINT         NULL,            -- matched or auto-created
    is_new_supply         BOOLEAN        NOT NULL DEFAULT FALSE,
    is_new_variant        BOOLEAN        NOT NULL DEFAULT FALSE,
    old_stock             DECIMAL(10,3)  NULL,
    new_stock             DECIMAL(10,3)  NULL,
    old_unit_cost         DECIMAL(10,2)  NULL,
    new_unit_cost         DECIMAL(10,2)  NULL,
    line_total            DECIMAL(12,2)  NOT NULL,
    CONSTRAINT fk_psi_session  FOREIGN KEY (session_id)          REFERENCES purchase_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_psi_supply  FOREIGN KEY (resolved_supply_id)  REFERENCES supplies(id)         ON DELETE SET NULL,
    CONSTRAINT fk_psi_variant FOREIGN KEY (resolved_variant_id) REFERENCES supply_variants(id) ON DELETE SET NULL
);

CREATE INDEX idx_psi_session ON purchase_session_items(session_id);
```

### 3.3 `purchase_order_items` enhancement

```sql
ALTER TABLE purchase_order_items
    ADD COLUMN purchase_session_item_id BIGINT NULL,
    ADD CONSTRAINT fk_poi_psi FOREIGN KEY (purchase_session_item_id)
        REFERENCES purchase_session_items(id) ON DELETE SET NULL;
```

This is what `SupplyVariant.last_purchase_item_id` (Plan 01) will reference.

---

## 4. Domain layer

### 4.1 New domain entities

```
core/purchase/domain/
  PurchaseSession.java          // status machine: PREVIEWED → CONFIRMED | CANCELLED | EXPIRED
  PurchaseSessionItem.java
  PurchasePreviewPayload.java   // immutable snapshot returned by PreviewPurchase
  PurchasePreviewLine.java      // old/new stock + old/new price + isNew flag
  PurchaseConfirmationResult.java
```

```java
public enum PurchaseSessionStatus {
  PREVIEWED, CONFIRMED, CANCELLED, EXPIRED;

  public boolean isTerminal() { return this != PREVIEWED; }
}
```

### 4.1.1 `PurchaseSession` (concurrency-safe state)

```java
public record PurchaseSession(
    Long id,
    String supplierName,
    String invoiceNumber,
    LocalDateTime purchasedAt,
    Long registeredBy,
    String payloadJson,                 // frozen FE payload
    String previewJson,                 // computed snapshot
    PurchaseSessionStatus status,
    Long purchaseOrderId,
    String confirmationIdempotencyKey, // null until confirm
    long version,                        // @Version mirror
    LocalDateTime createdAt,
    LocalDateTime confirmedAt,
    LocalDateTime expiresAt
) {}
```

The corresponding JPA entity carries:

```java
@Entity
@Table(name = "purchase_sessions")
public class PurchaseSessionEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PurchaseSessionStatus status;

  @Version
  @Column(nullable = false)
  private Long version;          // ← belt-and-suspenders with PESSIMISTIC_WRITE

  // ... other fields ...

  // Lock variants exposed by the adapter:
  // - findByIdForUpdate(id) → @Lock(PESSIMISTIC_WRITE) + @QueryHints lock timeout
  // - findByIdempotencyKeyForUpdate(key, status) → same
}
```

### 4.2 New exceptions (`application/exception/`)

- `PurchaseSessionNotFoundException` → 404
- `PurchaseSessionAlreadyConfirmedException` → 409
- `PurchaseSessionExpiredException` → 410 (Gone)
- `InconsistentPurchasePayloadException` → 400
- `EmptyPurchasePayloadException` → 400
- `AutoCreateSupplyForbiddenException` → 422 (when user disabled auto-create)
- `UnitOfMeasureAmbiguousException` → 422 (when raw unit cannot be matched)
- `PurchaseSessionImmutableException` → 409 (cancel/confirm on terminal state)

---

## 5. Application layer (use cases)

### 5.1 `PreviewPurchaseUseCase` (port in `domain/purchase/port/input/`)

```
PurchasePreviewPayload preview(PurchasePreviewCommand command)
```

Flow (pure logic, no DB writes to inventory/variants):

1. Validate payload: `supplier_name`, `purchased_at` non-blank; items array non-empty; each item has positive `quantity` and `unit_price`.
2. **Supplier matching**: lookup `suppliers` by name (case-insensitive, trimmed).
   - If found and `active = true` → link `supplierId`.
   - If found and `active = false` → `SupplierInactiveException` (422).
   - If not found → auto-create supplier (`active = true`, name = raw); flag `isNewSupplier = true` on the session.
3. **Insumo matching** per item (in input order):
   - For each `{insumo_name, unit, quantity, unit_price}`:
     a. Lookup `units_of_measure` by `name` or `abbreviation` (case-insensitive). Fail with `UnitOfMeasureAmbiguousException` if more than one matches.
     b. Lookup `supplies` by `name`. If absent:
        - Auto-create supply in a **default category** (configurable via `system_configuration.default_supply_category_id`; see Plan 04).
        - Auto-create `SupplyVariant` with `(supply, unit, quantity)` and `unit_cost = unit_price` (no previous stock to average).
        - Flag `isNewSupply = true`, `isNewVariant = true`.
     c. If supply exists but no variant matches `(unit, quantity)`:
        - Auto-create variant with `unit_cost = unit_price`, `stock = 0`.
        - Flag `isNewVariant = true`.
     d. Compute new weighted average using `WeightedAverageCalculator` from Plan 01:
        - `S_current` = current `inventory_stock.current_quantity` in `Bodega` (sum across all locations for safety; pick default if absent → 0).
        - `P_current` = current `supply_variants.unit_cost` (Money per Plan 06).
        - `Q_purchased = raw_quantity`.
        - `P_purchased = raw_unit_price`.
        - `P_new = compute(S_current, P_current, Q_purchased, P_purchased)`.
        - `S_new = S_current + Q_purchased`.
4. Build `PurchasePreviewPayload`:
   ```json
   {
     "sessionId": 987,
     "supplier": { "id": 12, "name": "Distribuidora X", "isNew": false },
     "purchasedAt": "2026-07-14T10:00:00Z",
     "invoiceNumber": "FAC-00123",
     "items": [
       {
         "rawName": "Tomato",
         "rawUnit": "lb",
         "rawQuantity": 5,
         "rawUnitPrice": 1.20,
         "resolved": {
            "supplyId": 17,
            "supplyName": "Tomato",
            "variantId": 42,
            "unit": { "id": 2, "abbreviation": "lb" },
            "quantity": 1.000,
            "isNewSupply": false,
            "isNewVariant": false
         },
         "before": { "stock": 3.000, "unitCost": 1.10 },
         "after":  { "stock": 8.000, "unitCost": 1.175 },
         "lineTotal": 6.00
       }
     ],
     "totals": { "lines": 1, "subtotal": 6.00, "currency": "COP" },
     "expiresAt": "2026-07-15T10:00:00Z"
   }
   ```
5. Persist the **preview state**: `purchase_sessions` (status=`PREVIEWED`) + `purchase_session_items` (resolved ids, old/new stock & cost). **NO mutation** of `supplies`, `supply_variants`, `inventory_stock`, `purchase_orders`.
6. Return the payload **and** the `sessionId`.

### 5.2 `ConfirmPurchaseUseCase`

```
PurchaseConfirmationResult confirm(ConfirmPurchaseCommand cmd)
```

```java
public record ConfirmPurchaseCommand(
    Long sessionId,
    String idempotencyKey,
    Long performedBy
) {}
```

Flow:

1. **Idempotency check** (cheap read, outside the heavy tx):
   - `findByIdempotencyKeyAndStatus(idempotencyKey, CONFIRMED)`.
   - If found → return the cached `PurchaseConfirmationResult` (rebuilt from session row + linked `purchase_order`). **No DB writes.**
2. Load session with `PESSIMISTIC_WRITE` + 5s timeout hint (`@QueryHints({@QueryHint(name="jakarta.persistence.lock.timeout", value="5000")})`). Reject if not `PREVIEWED` → `PurchaseSessionAlreadyConfirmedException` 409.
3. Validate `expires_at > NOW()` → else throw `PurchaseSessionExpiredException` 410.
4. Inside a single `@Transactional` (rolled back on any failure):
   a. **For each session item**, in order:
      - If `isNewSupply = true`: re-resolve `Supply` by name (might have been created in parallel preview).
      - If `isNewVariant = true`: re-resolve variant by `(supply, unit, quantity)`.
      - `PESSIMISTIC_WRITE` lock the `SupplyVariant` row (Plan 01 also adds `@Version` here).
      - Recompute `P_new` against **fresh** `S_current`, `P_current` (preview snapshot may be stale).
      - Update `supply_variants.unit_cost = P_new`, `last_cost_updated_at = NOW()`, `last_purchase_item_id = <new poi>`.
      - Lock + upsert `inventory_stock(variant_id, 'Bodega')`, add `raw_quantity`.
      - Insert `purchase_order_items` row linked to session item.
      - Insert `inventory_movements` with `MovementType.ENTRY`, `reference_purchase_order_id = new po`.
   b. Insert `purchase_orders` row (supplier_id, registered_by, purchased_at, total_amount = sum of line_totals, notes).
   c. Update session → `status = CONFIRMED`, `confirmed_at = NOW()`, `purchase_order_id = new po`, `confirmation_idempotency_key = cmd.idempotencyKey`.
      - Catch `OptimisticLockingFailureException` and translate to `PurchaseSessionConcurrentModificationException` 409 (caller should retry the read+confirm).
      - Catch `DataIntegrityViolationException` on `uq_ps_idemp_confirmed` → rebuild and return cached result (idempotent replay from a different code path).
5. Publish `InventoryStockUpdatedEvent` (WebSocket) for the affected variant/locations.
6. **Append audit log** — one row per confirmed purchase (financial-software standard):
   ```
   action:         PURCHASE_CONFIRMED
   entityType:     PURCHASE_SESSION
   entityId:       sessionId
   performedBy:    performedBy
   performedAt:    Instant.now()   -- UTC
   previousState:  { status: PREVIEWED, lineCount: n }
   newState:       { status: CONFIRMED, purchaseOrderId: poId, totalAmount: "..." }
   reason:         null            -- confirm has no manual reason
   ```
7. Return `PurchaseConfirmationResult { sessionId, purchaseOrderId, totalAmount, confirmedAt }`.

**Retry pattern** (Spring-recommended for optimistic-locking failures):

```java
// at the controller / REST boundary
@Retryable(
    retryFor = { ObjectOptimisticLockingFailureException.class,
                 PurchaseSessionConcurrentModificationException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 200, multiplier = 2.0)
)
public PurchaseConfirmationResult confirm(...) { ... }
```

### 5.3 `CancelPurchasePreviewUseCase`

```
void cancel(Long sessionId, Long performedBy)
```

Sets `status = CANCELLED`. No mutation. Idempotent.

### 5.4 `GetPurchasePreviewUseCase`

```
PurchasePreviewPayload findBySessionId(Long sessionId)
```

Returns the stored preview (re-builds from `purchase_sessions.payload_json` + `purchase_session_items`). Used by the FE if the user reloads the page mid-preview.

### 5.5 `ExpirePurchasePreviewsUseCase` (housekeeping)

```
int expireDue()
```

Sets `status = EXPIRED` for all `PREVIEWED` sessions with `expires_at < NOW()`. Invoked by the cron job from Plan 05.

---

## 6. Sequence diagrams

### 6.1 Preview flow

```
Client                FE Adapter             PreviewPurchaseUseCase        Domain Ports               DB
  |  POST /purchases/preview {payload}   |              |                       |                          |
  |------------------------------------> |              |                       |                          |
  |                                     | validate     |                       |                          |
  |                                     |------------> |  find supplier by name|                          |
  |                                     |              |---------------------->| suppliers                |
  |                                     |              |                       |                          |
  |                                     |              | for each item:        |                          |
  |                                     |              |   match UoM, supply   |                          |
  |                                     |              |   match variant       |                          |
  |                                     |              |   auto-create if miss |                          |
  |                                     |              |   compute P_new       |                          |
  |                                     |              |---------------------->| supply_variants (READ)   |
  |                                     |              |---------------------->| inventory_stock (READ)   |
  |                                     |              |                       |                          |
  |                                     |              | INSERT session (PREVIEWED, payload_json, preview_json)  |
  |                                     |              |---------------------->| purchase_sessions        |
  |                                     |              | INSERT items          |                          |
  |                                     |              |---------------------->| purchase_session_items   |
  |                                     |              |                       |                          |
  |  200 OK {preview, sessionId}       |              |                       |                          |
  |<------------------------------------|              |                       |                          |
```

### 6.2 Confirm flow (atomic)

```
Client        FE       ConfirmPurchaseUseCase        Tx Boundary         DB
  |  POST /purchases/{sessionId}/confirm  |                 |                |
  |-------------------------------------->|                 |                |
  |                                      | SELECT FOR UPDATE session         |
  |                                      |---------------------------------->|
  |                                      |  if not PREVIEWED → 409           |
  |                                      |  if expired    → 410              |
  |                                      |                 |                |
  |                                      | BEGIN @Transactional              |
  |                                      |   for each item:                  |
  |                                      |     SELECT FOR UPDATE supply_variant
  |                                      |     recompute P_new               |
  |                                      |     UPDATE supply_variants        |
  |                                      |     SELECT FOR UPDATE inventory_stock (Bodega)
  |                                      |     UPSERT inventory_stock        |
  |                                      |     INSERT purchase_order_items   |
  |                                      |     INSERT inventory_movements (ENTRY)
  |                                      |   INSERT purchase_orders          |
  |                                      |   UPDATE session → CONFIRMED      |
  |                                      | COMMIT                            |
  |                                      |  publish WS event                 |
  |                                      |  audit log                        |
  |  200 OK {purchaseOrderId, totalAmount}                |                |
  |<--------------------------------------|                 |                |
```

---

## 7. SQL pseudocode (weighted average update inside confirm)

```sql
-- Run inside the @Transactional boundary of ConfirmPurchaseUseCase
-- per line item. Variant row is already locked.

-- 1. Fetch fresh snapshot
SELECT unit_cost
INTO   v_current_price
FROM   supply_variants
WHERE  id = :variant_id;                 -- row already PESSIMISTIC_WRITE-locked

SELECT COALESCE(SUM(current_quantity), 0)
INTO   v_current_stock
FROM   inventory_stock
WHERE  supply_variant_id = :variant_id
  AND  storage_location_id = :bodega_id;

-- 2. Compute new weighted average
-- P_new = (S_current * P_current + Q * P_purchased) / (S_current + Q)
-- All math in DECIMAL(19,4) to avoid drift.

SET v_new_price =
    ROUND(
      ( v_current_stock * v_current_price + :qty * :unit_price )
      / NULLIF(v_current_stock + :qty, 0),
      2
    );

-- 3. Update the variant
UPDATE supply_variants
   SET unit_cost             = v_new_price,
       last_cost_updated_at  = NOW(),
       last_purchase_item_id = :poi_id
 WHERE id = :variant_id;

-- 4. Upsert stock in Bodega
INSERT INTO inventory_stock (supply_variant_id, storage_location_id, current_quantity)
VALUES (:variant_id, :bodega_id, :qty)
ON DUPLICATE KEY UPDATE
    current_quantity = current_quantity + VALUES(current_quantity);
-- (MySQL syntax; for PostgreSQL use INSERT ... ON CONFLICT DO UPDATE)
```

The whole block runs inside the same transaction; any failure rolls back all items atomically.

---

## 8. REST API surface

Base: `/api/v1/purchases`. All endpoints `@Tag(name = "Purchases")`.

| Method | Path | Purpose | Status |
|---|---|---|---|
| `POST` | `/purchases/preview` | Start preview session | 201 / 400 / 422 |
| `GET`  | `/purchases/previews/{sessionId}` | Retrieve stored preview | 200 / 404 |
| `POST` | `/purchases/previews/{sessionId}/confirm` | Confirm preview → write to DB | 200 / 404 / 409 / 410 |
| `POST` | `/purchases/previews/{sessionId}/cancel` | Discard preview | 204 / 404 / 409 |
| `GET`  | `/purchases/previews?status=PREVIEWED&page=0&size=20` | List sessions | 200 |
| `GET`  | `/purchases` | List confirmed purchase orders | 200 |
| `GET`  | `/purchases/{id}` | Get purchase order | 200 / 404 |

Request body for preview:

```json
{
  "supplierName": "Distribuidora El Sur",
  "invoiceNumber": "FAC-00123",
  "purchasedAt": "2026-07-14T10:00:00Z",
  "items": [
    { "insumoName": "Tomato", "unit": "lb", "quantity": 5,  "unitPrice": 1.20 },
    { "insumoName": "Onion",  "unit": "lb", "quantity": 3,  "unitPrice": 0.80 }
  ],
  "options": {
    "autoCreateSupplies": true,
    "autoCreateVariants": true
  }
}
```

Response body (preview) — see § 5.1.

Confirm request (header carries the key):

```http
POST /purchases/previews/987/confirm
Idempotency-Key: 5d8e...-2026-07-14
```

Response:

```json
{
  "sessionId": 987,
  "purchaseOrderId": 451,
  "totalAmount": { "amount": 8.40, "currency": "COP" },
  "confirmedAt": "2026-07-14T10:01:23Z"
}
```

---

## 9. Concurrency model

- **Variant lock**: `PESSIMISTIC_WRITE` + `@Version` on `supply_variants(id)` while updating `unit_cost` and `last_*`. Belt-and-suspenders: lock prevents concurrent SELECT-then-UPDATE; version catches any remaining race.
- **Stock lock**: `PESSIMISTIC_WRITE` on `inventory_stock(supply_variant_id, storage_location_id)` with `@QueryHints({@QueryHint(name="jakarta.persistence.lock.timeout", value="3000")})` so we fail fast instead of hanging the request.
- **Session lock**: `PESSIMISTIC_WRITE` + `@Version` on `purchase_sessions(id)` during confirm.
- **Isolation**: `READ_COMMITTED` (default). The locks above provide the necessary serialization; the `@Version` columns catch anything that slips through (e.g., a connection-pool timeout that lost the lock).
- **Idempotency**: clients may retry `POST /confirm` with the same `Idempotency-Key` header. The compound UNIQUE `(confirmation_idempotency_key, status)` guarantees only one confirm wins; retries with the same key find the cached session and return the cached `PurchaseConfirmationResult`.
- **Retry**: the boundary layer retries on `ObjectOptimisticLockingFailureException` up to 3 times with exponential backoff (`@Retryable`).

---

## 10. Edge cases

| Case | Handling |
|---|---|
| Incoming `insumo_name` not in DB | Auto-create supply + variant (default category); flagged in preview |
| Incoming `unit` not recognized | `UnitOfMeasureAmbiguousException` 422 — frontend must retry with canonical `abbreviation` |
| Concurrent previews for same supplier | Each session is independent; locks only apply at confirm time |
| Concurrent confirms on same session | UNIQUE `idempotency_key` + status check → second call sees `status = CONFIRMED` → 409 |
| Confirm after expiry | `PurchaseSessionExpiredException` 410; client must re-preview |
| Duplicate preview/confirm (network retry) | `Idempotency-Key` header → cached result |
| Empty items array | `EmptyPurchasePayloadException` 400 |
| Negative `quantity` or `unit_price` | `InconsistentPurchasePayloadException` 400 |
| Supplier exists but inactive | `SupplierInactiveException` 422 (auto-create new active supplier with `_v2` suffix instead — configurable) |
| `P_new` rounding drift | Intermediate `DECIMAL(19,4)`, final `DECIMAL(10,2) ROUND HALF_UP` |
| `Money` currency mismatch (Plan 06) | All money fields coerced to `system_configuration.default_currency`; mismatch → 422 |
| Cancelled session re-confirmed | `PurchaseSessionImmutableException` 409 |

---

## 11. Tests

| Test class | Coverage |
|---|---|
| `PreviewPurchaseServiceTest` | Match existing insumo; auto-create missing; weighted average math; reject empty payload |
| `PreviewPurchaseServiceTest` | Unit of measure not found → 422 |
| `ConfirmPurchaseServiceTest` | Atomic write; idempotency replay returns same result; rejects expired session |
| `ConfirmPurchaseServiceTest` | Rollback when one item fails (test exception injected in 3rd line) |
| `CancelPurchasePreviewServiceTest` | Status transition rules |
| `ExpirePurchasePreviewsServiceTest` | Flips only PREVIEWED rows past expiry |
| `PurchaseSessionConcurrencyIT` | Two parallel confirms → one succeeds, one returns 409 |
| `WeightedAverageCalculatorIT` | Real DB, real locks; proves P_new formula against seeded data |
| `PurchaseControllerIT` | Swagger / status codes / `@ApiResponse` |

---

## 12. Acceptance criteria

- [ ] `V31` + `V31_1` migrations create `purchase_sessions` + `purchase_session_items`, link `purchase_order_items`, and add `uq_ps_idemp_confirmed`.
- [ ] `purchase_sessions.version` is mapped with `@Version` on the entity.
- [ ] Pessimistic lock methods carry the `jakarta.persistence.lock.timeout = 5000` hint.
- [ ] `PreviewPurchaseUseCase` returns old vs new stock + cost + `isNewSupply/isNewVariant` flags.
- [ ] Auto-create insumo only fires when `options.autoCreateSupplies = true`.
- [ ] `ConfirmPurchaseUseCase` runs all mutations inside one `@Transactional` boundary; rollback tested.
- [ ] `ObjectOptimisticLockingFailureException` triggers `@Retryable` retry with backoff.
- [ ] Weighted average recompute uses `WeightedAverageCalculator` from Plan 01.
- [ ] Idempotency-Key supported on confirm (UNIQUE index on `(idempotency_key, status)`).
- [ ] Cron-driven `ExpirePurchasePreviewsUseCase` flips stale sessions (Plan 05).
- [ ] Money values use `Money` value object (Plan 06).
- [ ] `./harness/harness.sh` exits 0.

---

## 13. Risks / follow-ups

- **Stale preview snapshots**: a preview computed 23h59m ago might have drifted from current stock by the time confirm runs → always **re-read** fresh values inside confirm; the preview is only a UX hint.
- **Auto-create flood**: turning auto-create ON against a misconfigured OCR can flood `supplies`. Guard with `autoCreateSupplies = false` default until admin opts in.
- **Currency**: current schema assumes single currency (COP). Add `currency` column later if multi-currency is needed.
- **Old one-step endpoint**: keep `RegisterPurchaseOrderService` deprecated (return 410 with hint to use preview/confirm) until frontend migrates; remove in the next major release.