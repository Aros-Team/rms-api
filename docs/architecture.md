# Architecture

> What "good work" means in this project. Read before implementing any feature.

## Hexagonal Architecture (Ports & Adapters)

The codebase is strictly layered. The arrow of dependency points **inward**:

```
┌──────────────────────────────────────────────────────────┐
│  infrastructure/    ← REST, JPA, Security, Mail, etc.   │
│       │              (Spring, JPA, third-party libs)     │
│       ▼                                                  │
│  application/       ← Use case services (orchestration) │
│       │              (one class per verb+entity)         │
│       ▼                                                  │
│  domain/            ← Entities, value objects, ports     │
│       │              (NO framework imports)              │
└──────────────────────────────────────────────────────────┘
```

### Layer rules

| Layer | Allowed | Forbidden |
|-------|---------|-----------|
| `domain/` | Plain Java, `@Data`, `@Builder`, `Logger` interface | `org.springframework.*`, `jakarta.persistence.*`, any framework |
| `application/` | Domain types, ports from domain, `Logger` | Framework annotations, JPA |
| `infrastructure/` | Anything — this is the framework layer | Reaching back into `domain/` for *new* abstractions (extend existing ones instead) |

### Core MUST NOT depend on Infrastructure

This is non-negotiable. If you find yourself wanting `@Service` in `domain/`, **stop** — move the logic to `application/` and inject a port.

## Use Case Naming

Use specific, action-oriented names. Pattern: `{Verb}{Entity}UseCase`.

| Avoid | Use |
|-------|-----|
| `ProcessOrder` | `TakeOrder`, `CancelOrder`, `CompleteOrder` |
| `ManageTable` | `ReserveTable`, `ReleaseTable`, `UpdateTableStatus` |
| `UserService` | `CreateUser`, `AuthenticateUser`, `UpdateUserProfile` |
| `ProductService` | `CreateProduct`, `UpdateProduct`, `DeactivateProduct` |

Each use case = one class in `application/{domain}/`. Each has a matching port interface in `domain/{domain}/port/in/`.

## Exception Handling

### Business exceptions (core)

Live in `domain/{boundedcontext}/exception/` or `application/{boundedcontext}/exception/` (depending on where the rule lives). Extend `RuntimeException`. **No Spring annotations.**

### HTTP handling (infrastructure)

Single `@RestControllerAdvice` at `infrastructure/common/exception/GlobalExceptionHandler.java`.

### Naming → HTTP status mapping

| Exception suffix | HTTP status |
|------------------|-------------|
| `*NotFoundException` | 404 |
| `*AlreadyExistsException` | 409 |
| `Invalid*Exception` | 400 |
| `*BusinessException` | 500 |

## Logging

- **Domain**: depends on a `Logger` **interface** declared in domain. No implementation.
- **Application**: same `Logger` interface.
- **Infrastructure**: provides the implementation backed by `@Slf4j`. Wired at startup.

Never `System.out.println`. Never `LoggerFactory.getLogger()` outside `infrastructure/`.

## Database

- `@Entity` / `@Table` only in `infrastructure/persistence/entity/`.
- Repositories: `JpaRepository` only in `infrastructure/persistence/repository/`.
- Migrations: Flyway under `src/main/resources/db/migration/`. **Never** `ddl-auto`.
- Concurrency: `@Lock(LockModeType.PESSIMISTIC_WRITE)` on adapter methods that mutate critical rows, wrapped in `@Transactional`.

## API surface

- Every `@RestController` needs `@Tag`.
- Every endpoint needs `@Operation(summary = "...", description = "...")`.
- Document failure modes with `@ApiResponse`.
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>.

### Response contract (see `docs/contracts/api-envelope.md`)

- **2xx JSON**: always the single envelope `{ "data": <payload>, "meta": <optional> }`.
  Applied once by `ApiEnvelopeAdvice`; controllers return their DTOs, not the envelope.
- **4xx/5xx**: RFC 7807 `application/problem+json` (`ProblemDetail`) with `type`, `title`,
  `status`, `detail`, and `instance`. Errors are never enveloped. Filter-level 401/403 use the same
  shape (401 keeps `WWW-Authenticate`).
- **204 / binary / `/.well-known/jwks.json`**: not enveloped.
- **Money**: only `MoneyDto` — `{ "amount": "123.45", "currency": "COP" }`, `amount` always a
  decimal **string**. Percentages/ratios/quantities stay numeric.
- OpenAPI is adjusted by `ApiEnvelopeOpenApiCustomizer` (including `*/*` media types);
  `ApiDocsEnvelopeIntegrationTest` fails if any 2xx operation is published flat. Never wrap a body
  manually.

## Build & run

| Activity | Command |
|----------|---------|
| Run app | `task run` or `harness/activities/run.yml` (`docker compose up -d` + `gradlew bootRun`) |
| Build image | `task build` |
| Run tests | `task test` or `harness/activities/test.yml` |
| Format code | `task format` |
| Clean | `task clean` |
| Generate JWT keys | `task jwtkeys` |

The harness (`./harness/harness.sh`) verifies the environment and runs the same quality pipeline.