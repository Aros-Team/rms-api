**Use task commands**
```bash
# Start app (Docker + Spring Boot)
task run
# Run tests
task test
# Clean and restart
task clean
```
---
**Always run after Java changes:**
```bash
task format
```
---
**Project Architecture: Hexagonal**
---
**CRITICAL: Core must NOT depend on Infrastructure**
---
### Core Layer (Business Logic - Framework Agnostic)
- Domain entities and value objects
- Input ports (use case interfaces)
- Output ports (repository interfaces)
- Business exceptions
- Logger interface (no implementation)

**PROHIBITED in Core:**
- Spring annotations (`@Service`, `@Repository`, `@Component`)
- JPA annotations (`@Entity`, `@Table`)
- Any `org.springframework.*` imports

### Infrastructure Layer (Framework Implementations)

## Use Case Naming

Use specific, action-oriented names:

| Avoid | Use |
|-------|-----|
| `ProcessOrder` | `TakeOrder`, `CancelOrder`, `CompleteOrder` |
| `ManageTable` | `ReserveTable`, `ReleaseTable`, `UpdateTableStatus` |
| `UserService` | `CreateUser`, `AuthenticateUser`, `UpdateUserProfile` |

**Pattern**: `{Verb}{Entity}UseCase` (e.g., `TakeOrderUseCase`)

## Lombok Usage

- `@Data`: Simple DTOs/entities
- `@Builder`: Complex objects
- `@Builder.Default`: Fields with initial values
- `@Slf4j`: **Only in infrastructure layer** (never in core)
- `@AllArgsConstructor`, `@NoArgsConstructor`: As needed

## Exception Handling

### Business Exceptions (Core)
- Location: `core/{domain}/application/exception/`
- Extend `RuntimeException`
- NO Spring annotations in core

### HTTP Handling (Infrastructure)
- Location: `infraestructure/common/exception/GlobalExceptionHandler.java`
- Single `@RestControllerAdvice` for all exceptions

### Naming Convention
- `{Entity}NotFoundException` → 404
- `{Entity}AlreadyExistsException` → 409
- `Invalid{Entity}Exception` → 400
- `{Business}Exception` → 500

## Logging

### Rules
- **Core**: Use `Logger` interface (no implementation)
- **Infrastructure**: Implement `Logger` with `@Slf4j`
- Inject logger into use cases that need it

## Swagger / OpenAPI

### Rules
- Every `@RestController` needs `@Tag`
- Every endpoint needs `@Operation` with summary and description
- Use `@ApiResponse` to document possible response codes
- Group endpoints by domain using tags

## Checkstyle Rules

### PROHIBITED: Text blocks in @Schema
Do not use `"""..."""` in `@Schema(example = """..."""`. Checkstyle 13.4.1 bug NPE.

**CORRECT:** `@Schema(example = "{\"name\": \"Kitchen\"}")`
**WRONT:** `@Schema(example = """{"name": "Kitchen"}""")`

## Database

- Use `@Entity` and `@Table` for JPA entities
- Define relationships with `@OneToMany`, `@ManyToOne`
- Extend `JpaRepository` for repositories
- Prefer JPQL over native queries
- Use Flyway for migrations (do not use `ddl-auto`)

## Security

- Use Spring Security for authentication/authorization
- NEVER hardcode secrets - use environment variables
- Validate all inputs with `@Valid` and Bean Validation
- Never expose stack traces to clients

## Concurrency

For atomic operations requiring race condition prevention:
- Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` in repository
- Method must be within `@Transactional` in adapter

## Never use build commands.
