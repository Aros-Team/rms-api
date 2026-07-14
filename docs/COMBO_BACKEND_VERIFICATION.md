# Backend Verification — Combo (Special Selection) Refactor

> Status report against the 12 questions raised for the FE team. Investigated by reading the API source directly (commit `1827c92`). Use this to align FE expectations with backend reality.

---

## 1. Combo groups structure

✅ **Confirmed.** Group shape is `{ id, categoryId, displayOrder, required, minSelections, maxSelections, productIds }`.

- No `name` field.
- No `optionIds` field.
- No nested options array.
- `id: null` is accepted for new groups (validated in `SpecialSelectionValidator.validateConfiguration`).
- File: `src/main/java/aros/services/rms/infraestructure/specialselection/api/dto/SpecialSelectionGroupDto.java`
- Domain: `src/main/java/aros/services/rms/core/specialselection/domain/SpecialSelectionGroup.java`

---

## 2. Suggested-price endpoint

⚠️ **Mismatch.** Backend exposes:

```
POST /api/v1/admin/special-selections/{productId}/suggest-price
Content-Type: application/json
Body: { "marginPercent": 30 }
```

- Method is **POST**, not GET.
- Path is **`/{productId}/suggest-price`**, not `/suggested-price`.
- The new-spec proposal `GET /api/v1/special-selections/suggested-price` would need a new endpoint.
- File: `src/main/java/aros/services/rms/infraestructure/specialselection/api/SpecialSelectionAdminController.java` (lines 264–272)

---

## 3. Suggested-price breakdown shape

⚠️ **Mismatch.** Backend returns:

```jsonc
{
  "suggestedPrice": 15.50,
  "totalCost": 10.85,
  "marginPercent": 30.0,
  "breakdown": [
    { "optionId": null, "name": "product:101", "cost": 1.20 },
    { "optionId": null, "name": "base:42",     "cost": 0.50 }
  ]
}
```

- `optionId` is **always null** for the new combo approach (legacy field kept for backwards compat).
- `name` is a synthetic label (`"product:<productId>"` or `"base:<variantId>"`) — **not a human-readable product name**.
- No `productId` field on each breakdown item.
- File: `SuggestedPriceResponse.java` + `SpecialSelectionPricingService.java` lines 100–133

---

## 4. GET /api/v1/products — shape & filter

⚠️ **Mismatch.** Backend endpoint signature:

```
GET /api/v1/products?categories=1,2&page=0&size=20&includeInactive=false
```

- ❌ **No `includeSelections=true` parameter.** The filter for SPECIAL_SELECTION products does not exist.
- Filters available: `categories` (List<Long>), `page`, `size`, `includeInactive`.
- `ProductResponse` does **NOT** expose `selectionType` or `imageUrl`.
- Returned fields: `id, name, description, basePrice, active, categoryId, categoryName, areaId, areaName, recipe, estimatedPrepMinutes`.
- File: `ProductController.java` lines 158–203, `ProductResponse.java`

---

## 5. Order request — `selectedProductIds` separate

✅ **Confirmed for POST and PUT.**

- `POST /api/v1/orders` accepts `selectedProductIds` as a separate field alongside `selectedOptionIds`.
- `PUT /api/v1/orders/{id}` (update) also accepts both — same `TakeOrderRequest` body.
- DTO: `TakeOrderRequest.OrderDetailRequest` has both fields.
- File: `OrderController.java` lines 82–109 (create), 168–192 (update)

---

## 6. Read-side order responses

⚠️ **Backend gap.** `OrderResponse.OrderDetailResponse` currently exposes:

```jsonc
{
  "id": 1,
  "productId": 7,
  "productName": "Menú ejecutivo",
  "unitPrice": 12.50,
  "instructions": "Sin sal",
  "selectedOptions": [{ "id": 10, "name": "Queso", "categoryName": "Extras" }]
}
```

- ❌ Does **NOT** include `selectedProductIds` (combo selections).
- ❌ Does **NOT** include `additionIds` or resolved addition names/prices.
- ❌ Does **NOT** include `clarifications` (questions + answers).
- ❌ Does **NOT** include category/group names.
- The `OrderDetail` domain **does** store all these fields, but the response DTO drops them.
- Kitchen/admin/waiter screens currently cannot see combo details from the order response.
- File: `OrderResponse.java` — needs additions before FE can render combo ticket data.

---

## 7. Question support — text only

⚠️ **Text-only.** Backend model is:

```java
class SpecialSelectionQuestion {
  Long id;
  Long productId;
  String question;     // free text
  boolean required;
  int displayOrder;
}
```

- Only free-text questions supported.
- ClarificationAnswer in orders is `{ questionId, answer }` — also free-text.
- No multiple-choice, no boolean, no structured options.
- File: `SpecialSelectionQuestion.java`, `ClarificationAnswer.java`

---

## 8. Top-level `Required` vs per-group

⚠️ **Per-group only.** The combo-level (`SpecialSelectionConfiguration`) does **NOT** have a `required` flag.

- The `required` field exists only on each `SpecialSelectionGroup`.
- `SpecialSelectionConfiguration` has: `productId, name, description, basePrice, active, preparationAreaId, selectionType, baseRecipeEnabled, schedulingRequired, groups, additions, questions, schedule`.
- File: `SpecialSelectionConfiguration.java`

---

## 9. Active toggle — full PUT only

⚠️ **No dedicated PATCH for active.** Endpoints on `/api/v1/admin/special-selections/{productId}`:

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/` | Create |
| PUT | `/{productId}` | Replace full config (use this to toggle `active`) |
| PUT | `/{productId}/price` | Patch price only |
| PUT | `/{productId}/schedule` | Patch schedule only |
| DELETE | `/{productId}` | Soft delete (sets `active=false`) |
| POST | `/{productId}/revert/{version}` | Revert |
| POST | `/{productId}/suggest-price` | Suggested price |

- No `PATCH /{productId}/active` or `/{productId}/toggle` endpoint exists.
- To toggle active: either full PUT or DELETE (soft-delete).
- File: `SpecialSelectionAdminController.java`

---

## 10. WebSocket payload

⚠️ **No envelope, no `changeType` field.** The topic `/topic/special-selections/updated` sends the **raw `SpecialSelectionResponse` object**:

```jsonc
{
  "productId": 7,
  "name": "Menú ejecutivo",
  "basePrice": 12.50,
  "active": true,
  "groups": [...],
  "additions": [...],
  "questions": [...],
  "schedule": [...]
}
```

- No `changeType` (CREATE / UPDATE / DELETE / PRICE_CHANGE / SCHEDULE_CHANGE / REVERT).
- No envelope `{ changeType, productId, selection }`.
- Subscribers cannot distinguish event type — they only see the new state.
- Note: `DELETE` calls `notifySpecialSelectionUpdated` with the latest saved config (still includes `active: false` after soft-delete). There is **no** message with `selection: null` for hard delete.
- File: `SpecialSelectionNotificationService.java`

---

## 11. HTTP error when combo unavailable mid-order

⚠️ **Not enforced.** Schedule validation is **NOT performed** when taking or updating an order.

- `TakeOrderService` and `UpdateOrderService` only call `SpecialSelectionValidator.validateOrderSelections()` (group/min/max/additions/questions).
- Neither calls `SpecialSelectionAvailabilityService.isAvailable(config, now)`.
- `SpecialSelectionAvailabilityService` exists but is only used by `GET /api/v1/special-selections/available`.
- Implication: a waiter can place a combo order outside its scheduled window, and a combo can be deactivated while in the order dock without the backend returning 409/422.
- File: `TakeOrderService.java`, `UpdateOrderService.java`

---

## 12. Legacy admin order creation

✅ **Single endpoint, full payload always required.**

- No separate "admin order" endpoint. Admin and waiter both use `POST /api/v1/orders`.
- The `TakeOrderRequest.OrderDetailRequest` payload is the same for both — `selectedProductIds` is a regular optional field on the request, so admin-created orders can include it.
- File: `OrderController.java`

---

## Summary of gaps that need backend changes

| # | Gap | Severity | Suggested fix |
|---|-----|----------|---------------|
| 2 | suggest-price endpoint is POST, not GET | Minor (FE can call POST) | Either add GET variant or keep POST; align FE |
| 3 | Breakdown lacks `productId` and human name | Medium | Enrich `SpecialSelectionPricingService` to lookup product names |
| 4 | `includeSelections=true` filter missing; `ProductResponse` lacks `selectionType`/`imageUrl` | Medium | Add filter + expose fields |
| 6 | Order response drops combo additions/clarifications/products | **High** | Extend `OrderResponse.OrderDetailResponse` |
| 7 | Questions are text-only | Low (depends on UX scope) | Add type discriminator (TEXT/CHOICE/BOOLEAN) |
| 9 | No dedicated active-toggle endpoint | Low | Add `PATCH /{productId}/active` |
| 10 | WebSocket payload lacks `changeType` envelope | Medium | Wrap payload + add `changeType` |
| 11 | Schedule not enforced on order create/update | **High** | Call `availabilityService.isAvailable()` in `TakeOrderService`/`UpdateOrderService` and return 409 when outside schedule |

Items 1, 5, 8, 12 are aligned and require no backend changes.