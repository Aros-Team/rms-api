# Conventions

> Style rules, names, structure. Read before writing code.

## Package layout

```
co.spalaxd.uros.rms.{context}/
├── domain/
│   ├── model/             # entities, value objects
│   ├── exception/         # *NotFound, *AlreadyExists, Invalid*
│   ├── port/
│   │   ├── in/            # use case interfaces
│   │   └── out/           # repository interfaces
│   └── util/              # Logger interface, pure helpers
├── application/
│   ├── service/           # @Service use case impls
│   └── exception/         # business exceptions
└── infrastructure/
    ├── rest/              # @RestController, DTOs, mappers
    ├── persistence/
    │   ├── entity/        # @Entity
    │   └── repository/    # JpaRepository
    ├── security/          # Spring Security config
    └── common/exception/  # GlobalExceptionHandler
```

## Lombok

| Annotation | When |
|-----------|------|
| `@Data` | Simple DTOs and entities with no custom behavior |
| `@Builder` | Complex objects with many fields, especially DTOs |
| `@Builder.Default` | Fields with non-null initial values |
| `@AllArgsConstructor` / `@NoArgsConstructor` | As needed |
| `@Slf4j` | **Infrastructure layer only** — never in `domain/` or `application/` |

Don't sprinkle `@Data` everywhere. If you need behavior, write it explicitly.

## Naming

### Classes

- Use cases: `{Verb}{Entity}UseCase` — `TakeOrderUseCase`, `CreateUserUseCase`.
- Port interfaces: `{Verb}{Entity}Port` for inputs, `{Entity}RepositoryPort` for outputs.
- Adapters: `{Technology}{Purpose}Adapter` — `JpaOrderRepositoryAdapter`, `JwtAuthenticationAdapter`.
- Controllers: `{Entity}Controller` — `OrderController`, `UserController`.
- Exceptions: see architecture.md.

### Methods

- Verbs, not nouns: `take()`, `cancel()`, `findById()`, `findByTableId()`.
- Boolean returns: `is*`, `has*`, `can*` — never `get*Active()`.
- Streams: avoid `getList()` — name by content: `findActiveOrders()`.

### DTOs

- Request: `{Action}{Entity}Request` — `TakeOrderRequest`, `UpdateProductRequest`.
- Response: `{Entity}Response` — `OrderResponse`, `ProductResponse`.

## Validation

- All `@RequestBody` parameters carry `@Valid`.
- Bean Validation annotations on DTOs (`@NotNull`, `@NotBlank`, `@Size`, etc.).
- Domain-level invariants validated in domain constructors (fail fast).

## Exception handling rules

- Never swallow exceptions silently.
- Never expose stack traces to clients (handled by `GlobalExceptionHandler`).
- Never catch `Exception` unless rethrowing or converting to a domain exception.

## Swagger / OpenAPI

- Every `@RestController` carries `@Tag(name = "...")`.
- Every endpoint carries `@Operation(summary = "...", description = "...")`.
- Document error responses with `@ApiResponse(responseCode = "...", description = "...")`.
- Group endpoints by bounded context.

### Checkstyle trap

**Do NOT** use text blocks inside `@Schema(example = """...""")` — Checkstyle 13.4.1 has an NPE bug.

```java
// ✅ correct
@Schema(example = "{\"name\": \"Kitchen\"}")

// ❌ wrong (NPE in Checkstyle)
@Schema(example = """
    {"name": "Kitchen"}
    """)
```

## Imports

- No wildcard imports (`co.spalaxd.*`).
- Group: `java.*`, `jakarta.*`, `org.springframework.*`, third-party, project.
- Sort alphabetically within group (Spotless handles this).
- **Never** import `org.springframework.*` in `domain/`.

## Testing

- Unit tests in `src/test/java/...` mirroring the production package.
- Use descriptive names: `should_reject_order_when_table_already_busy()`.
- One assertion concept per test (multiple `assertEquals` on related fields are fine).
- Always run `harness/activities/test.yml` (or `./gradlew test`) before declaring a task `done`.

## Git

- Branch: `feat/<scope>-<short-desc>`, `fix/<scope>-<short-desc>`, `chore/<scope>-<short-desc>`.
- Commits: imperative, lowercase, scoped — `feat(order): reject double-take`.
- Squash before merge. No "WIP" commits in main.
- Never commit `build/`, `.gradle/`, `.env`, `*.log`, `bin/`, `uploads/`, generated JWT keys.