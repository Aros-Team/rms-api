# Payments - Charge and Cash Shift

> Minimal charge-and-close (Option B) plus a minimal cash shift.
> Design source: `docs/sprint-0/payments-spike.md`, decision D-009.

## 1. Scope

v1 lets a cashier:

- charge a delivered order with one or more tenders (cash, card, transfer),
- add one manual discount with a reason and a tip,
- give cash change,
- reprint an internal receipt,
- void a charge while the shift is open,
- open and close a cash drawer shift and reconcile expected vs counted cash.

Out of scope (documented, not silently dropped): partial refunds, item-level split, card gateway,
fiscal invoice/DIAN numbering, drawer hardware, manual `PAID_IN`/`PAID_OUT` movements (the table
ships, the endpoint is v1.1), multi-terminal, discount approvals, offline mode and analytics
changes to net sales.

## 2. Aggregates

```
orders (unchanged)  1 ──── 0..1 payments 1 ──── 0..n payment_tenders
                              │
cash_shifts 1 ──── 0..n ──────┘
     │
     └──── 0..n cash_movements      (v1.1 ledger)
```

No `OrderStatus` value is added. An order stays `DELIVERED`; the cashier state is derived from the
presence of a non-voided payment. Table release is still owned by delivery and is not coupled to
payment.

| Derived label | Order status | Payment |
|---|---|---|
| not listed | `QUEUE`, `PREPARING`, `READY`, `CANCELLED` | any |
| Por cobrar | `DELIVERED` | none |
| Cobrada | `DELIVERED` | `COMPLETED` |
| Por cobrar (anulada) | `DELIVERED` | only `VOIDED` |

## 3. Lifecycles

Payment:

```
(none) ──POST /payments──> COMPLETED ──POST /payments/{id}/void──> VOIDED
                              ▲                                      │
                              └──── re-charge creates a new row ◄────┘
```

A `COMPLETED` payment is an immutable money snapshot. A void keeps the row for audit and is only
allowed while its shift is `OPEN`.

Cash shift:

```
(no shift) ──POST /cash-shifts/open──> OPEN ──POST /cash-shifts/{id}/close──> CLOSED
                                        │
                                        ├── charge
                                        └── void
```

Only one shift may be `OPEN`; a closed shift cannot be reopened or mutated in v1.

## 4. Endpoints

All paths are under `/api/v1`. Success bodies are wrapped as `{ "data": ... }`; errors are RFC 7807
`application/problem+json`. Money always uses `MoneyDto` (`amount` as a decimal string).

| Method | Path | Purpose | Success |
|---|---|---|---|
| `GET` | `/payments/pending?date=YYYY-MM-DD&search=&page=&size=` | Delivered orders without an active payment | `200` `PageResponse<PendingOrderResponse>` |
| `POST` | `/payments` | Charge an order (`Idempotency-Key` required) | `201` new / `200` replay |
| `GET` | `/payments/{id}` | Payment and receipt | `200` `PaymentResponse` |
| `GET` | `/payments?date=&method=&status=&page=&size=` | Paged payment journal | `200` `PageResponse<PaymentSummaryResponse>` |
| `POST` | `/payments/{id}/void` | Full void with reason | `200` `PaymentResponse` |
| `GET` | `/cash-shifts/current` | Current open shift | `200` or `204` |
| `POST` | `/cash-shifts/open` | Open with a float | `201` `CashShiftResponse` |
| `POST` | `/cash-shifts/{id}/close` | Close with counted cash | `200` `CashShiftSummaryResponse` |
| `GET` | `/cash-shifts/{id}` | Z-summary / expected vs counted | `200` `CashShiftSummaryResponse` |

### 4.1 Charge

```http
POST /api/v1/payments
Idempotency-Key: 3f1c9d2e-...
Content-Type: application/json

{
  "orderId": 201767,
  "tipAmount": "5000.00",
  "discount": { "type": "PERCENT", "value": "10.00", "reason": "Cortesia gerencia" },
  "tenders": [
    { "method": "CARD", "amount": "40000.00", "reference": "AUTH-4421" },
    { "method": "CASH", "amount": "20000.00" }
  ]
}
```

The server computes the subtotal from `order_details.unit_price`, applies the discount, adds the
tip and validates the tenders. There is no `subtotal` or `total` field in the request.

Internally all amounts are `Money`. A percentage discount is converted to integer basis points
(1 bp = 0.01%) before the domain calculation; the wire contract still accepts a decimal `value`.

### 4.2 Pending orders

`date` defaults to today and is matched against `orders.date` in the business timezone
(`[date 00:00, date+1 00:00)`). `search` is an exact order id or table number.

### 4.3 Void

```json
{ "reason": "Cobro duplicado en mesa 3" }
```

The response is the same payment with `status = VOIDED`, `voidedAt`, `voidedBy` and `voidReason`.
A void after the shift is closed returns `409`.

### 4.4 Cash shift close

```json
{ "countedCash": "790000.00", "notes": "Cierre turno noche" }
```

The summary returns the method breakdown, `expectedCash`, `countedCash` and `difference`.

```
expectedCash = openingFloat
             + Σ cash tenders of COMPLETED payments in the shift
             − Σ change_amount of those payments
             + Σ PAID_IN
             − Σ PAID_OUT
difference   = countedCash − expectedCash
```

`expectedCash`, `countedCash` and `difference` are snapshotted on the shift row at close so the
Z-report never changes retroactively.

## 5. Invariants

| # | Invariant | Enforcement |
|---|---|---|
| I-01 | At most one non-voided payment per order | Generated `active_order_id` + unique index; order row `PESSIMISTIC_WRITE` while charging |
| I-02 | Subtotal equals the server-recomputed order total | `PayableOrderPersistenceAdapter`; client cannot send a subtotal |
| I-03 | `total = subtotal − discount + tip` | `PaymentCalculator` |
| I-04 | `tendered = Σ tenders` and `change = tendered − total ≥ 0` | `PaymentCalculator` |
| I-05 | Only cash may exceed the remaining amount, and only as the last tender | `PaymentCalculator` |
| I-06 | `0 ≤ discount < subtotal`, `tip ≥ 0` | `PaymentCalculator` + DB constraints |
| I-07 | Only `DELIVERED` orders can be charged | `RegisterPaymentService` → `OrderNotPayableException` |
| I-08 | Payment currency equals the order currency | Service uses the order currency; `Money` rejects mismatches |
| I-09 | A charge requires an open shift | `RegisterPaymentService` → `ShiftNotOpenException`; FK |
| I-10 | Void is all-or-nothing, needs a reason, keeps the row | `VoidPaymentService`; audit aspect |
| I-11 | A voided payment cannot be voided again; a closed shift blocks charge/void | State guards |
| I-12 | Replaying an `Idempotency-Key` returns the original payment | Unique key + lookup after the order lock |
| I-13 | Money math uses `Money`/`BigDecimal` only | Domain model |

One open shift is enforced by the generated `open_flag` column and `uk_cash_shifts_open`.

## 6. Pending-order query

The cashier board runs this JPQL query (`PendingOrderJpaRepository.findPending`) inside the payments
module:

```
SELECT o FROM Order o LEFT JOIN o.table t
WHERE o.status = 'DELIVERED'
  AND o.date >= :start AND o.date < :end
  AND NOT EXISTS (
      SELECT p.id FROM PaymentEntity p
      WHERE p.orderId = o.id AND p.status <> 'VOIDED'
  )
  AND (:searchId IS NULL OR o.id = :searchId OR t.tableNumber = :searchNumber)
ORDER BY o.date ASC
```

Equivalent SQL:

```sql
SELECT o.*
FROM orders o
LEFT JOIN tables t ON t.id = o.table_id
WHERE o.status = 'DELIVERED'
  AND o.date >= :start AND o.date < :end
  AND NOT EXISTS (
      SELECT 1 FROM payments p
      WHERE p.order_id = o.id AND p.status <> 'VOIDED'
  )
  AND (:search_id IS NULL OR o.id = :search_id OR t.table_number = :search_number)
ORDER BY o.date ASC
LIMIT :size OFFSET :offset;
```

The total is recomputed from `order_details.unit_price`; `paymentStatus` is `VOIDED` when the only
payments for the order are voided, otherwise `UNPAID`.

## 7. Authorization

`@JustCashierOrAdmin` allows:

- `ADMIN`, or
- a `WORKER` whose `areaTypes` JWT claim contains `CASH`.

The annotation uses the existing `SecurityExpressions.hasArea` helper; `SecurityConfig` is not
modified. Reads are protected too: money data is not public inside the restaurant. Unauthorized
authenticated users receive `403` problem+json; anonymous callers receive `401`.

## 8. Migrations

| File | Content |
|---|---|
| `V50__create_cash_shifts.sql` | `cash_shifts`, one-open-shift generated index |
| `V51__create_payments.sql` | `payments`, unique active order, unique idempotency key, receipt |
| `V52__create_payment_tenders.sql` | `payment_tenders`, method/amount checks |
| `V53__create_cash_movements.sql` | `cash_movements` ledger (v1.1 endpoint) |

No existing table is altered.

## 9. Errors

| Exception | Status |
|---|---|
| `InvalidPaymentException` | 400 |
| `PaymentNotFoundException` | 404 |
| `CashShiftNotFoundException` | 404 |
| `OrderNotPayableException` | 409 |
| `OrderAlreadyPaidException` | 409 (+ `paymentId` property) |
| `PaymentAlreadyVoidedException` | 409 |
| `ShiftAlreadyOpenException` | 409 |
| `ShiftNotOpenException` | 409 |
| `ShiftClosedException` | 409 |
| `InvalidCashShiftException` | 400 |

## 10. Reporting note

Sales analytics stay order-based in v1: `net = gross = Σ order_details.unit_price`. Discounts and
tips are available only from `payments`. Changing the monthly aggregation is a Phase 6 decision.

## 11. Cashier UI contract

- Use `GET /payments/pending` as the board (paged, date + exact id/table search).
- Send one UUID per charge attempt in `Idempotency-Key`; reuse it on retry, never on a new attempt.
- On `409 OrderAlreadyPaid`, read `paymentId` from the problem body and open that receipt.
- On `409 ShiftNotOpen`, route to "Abrir caja".
- Disable the confirm button while the request is in flight; show `change` before confirm.
- The receipt view is the print source; there is no PDF or fiscal invoice in v1.
