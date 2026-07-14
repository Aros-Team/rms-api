# Task c — on-the-fly cost calculation — RESULT

## Files modified/created

### Modified
- `src/main/java/aros/services/rms/infraestructure/user/persistence/jpa/JpaUserRepository.java`
  - Added `findActiveByAreaId(Long areaId)` query method using JPQL with `JOIN u.assignedAreas a`.
- `src/main/java/aros/services/rms/infraestructure/user/persistence/jpa/UserRepositoryAdapter.java`
  - Implemented `findActiveByAreaId(Long)` delegating to internal JPA repo and mapping to domain.
- `src/main/java/aros/services/rms/infraestructure/product/api/ProductController.java`
  - Injected `CalculateProductCostUseCase`.
  - Added `GET /{id}/cost` endpoint returning `ProductCostResponse`.
- `src/main/java/aros/services/rms/infraestructure/product/config/ProductConfigBeans.java`
  - Added `@Bean` factory for `CalculateProductCostUseCase` wiring the new service.
- `src/main/java/aros/services/rms/core/product/domain/ProductCost.java`
  - Added Javadocs to `CostBreakdownItem` record + canonical constructor and `ProductCost` canonical constructor (fixup needed for checkstyle).

### Created
- `src/main/java/aros/services/rms/core/product/application/service/CalculateProductCostService.java`
  - New use case impl: material cost from recipe * supply unitCost; labor cost from avg worker hourly rate * (prep minutes / 60).
  - Uses `ProductRepositoryPort`, `ProductRecipeRepositoryPort`, `SupplyVariantRepositoryPort`, `UserRepositoryPort`, `Logger`.
  - Returns `ProductCost` with `totalCost`, `materialCost`, `laborCost`, `breakdown[]`.
- `src/main/java/aros/services/rms/infraestructure/product/api/dto/ProductCostResponse.java`
  - New DTO + nested `CostItem` record; static `fromDomain(ProductCost)` mapper.

## Build status
- `./gradlew spotlessApply` — OK
- `./gradlew compileJava` — OK (no errors)
- `./gradlew compileTestJava` — OK
- `./gradlew checkstyleMain` — OK (after ProductCost Javadoc fixup)
- `./gradlew test --tests "*Calculate*" --tests "*Product*Cost*"` — no matching tests (expected: cost calc has no tests yet)

## Harness sections 4–7 status
- §4 Code Quality — `[OK]`
- §5 Compilation — `[OK]`
- §6 Hexagonal Layering — `[OK]` (domain/ has no Spring/JPA imports)
- §7 Tests — `[OK]`

§3 reports "Found 2 activities in in_progress (max 1)" — out of scope for this task (orchestrator state).

## Notes
- `ProductNotFoundException` already exists at `src/main/java/aros/services/rms/core/product/application/exception/ProductNotFoundException.java` — used as-is.
- All new code respects hexagonal layering: `CalculateProductCostService` is in `core/product/application/service` and only depends on ports + domain types + `Logger` (no Spring/JPA imports).
- Labor calculation gracefully handles missing `preparationAreaId`, no paid workers in area, or missing `estimatedPrepMinutes` (emits a zero-cost breakdown item).