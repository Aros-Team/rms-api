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

---