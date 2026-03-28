# AGENTS.md - Agentic Coding Guidelines

This document provides guidelines for agents working on the RMS (Restaurant Management System) API codebase.

## Project Overview

- **Framework**: Spring Boot 4.0.3 with Java 21
- **Build Tool**: Gradle 9.3.x
- **Database**: MySQL 8.0
- **Key Dependencies**: Spring Data JPA, Spring Security, Spring Validation, Spring WebMVC, Spring WebSocket, Lombok, Flyway, MapStruct

## Build Commands

### Using Taskfile (Recommended for Development)
```bash
# Start app (Docker + Spring Boot)
task run

# Build and create Docker image
task build

# Format code
task format

# Run tests
task test

# Clean and restart
task clean
```

### Using Gradle Directly
```bash
# Build JAR without tests
./gradlew assemble

# Run Spring Boot
./gradlew bootRun

# Build Docker image
./gradlew bootJar
docker build -t rms-api:latest .

# Clean build directory
./gradlew clean
```

## Testing Commands

**Always run after Java changes:**
```bash
# Run all tests
./gradlew test

# Run single test class
./gradlew test --tests "ClassNameTests"

# Run single test method
./gradlew test --tests "ClassNameTests.methodName"

# Run with specific Spring profile
./gradlew test -Dspring.profiles.active=dev

# Run tests with verbose output
./gradlew test --info
```

## Code Style

**Always run after Java changes:**
```bash
task format
```

### Formatting Rules
- **Formatter**: Google Java Format via Spotless
- **Import Order**: java > javax > org > com > all other packages
- **No wildcard imports** (except static imports)
- **No unused imports**
- **Format annotations** before formatting code

### Naming Conventions
- **Classes**: PascalCase (`OrderService`, `MenuItemController`)
- **Methods**: camelCase (`findById`, `processOrder`)
- **Variables**: camelCase (`orderList`, `maxItems`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **Packages**: lowercase with dots (`aros.services.rms.controller`)

### Types and Generics
- Use generics: `List<Order>` not `List`
- Prefer interfaces: `List<T>` not `ArrayList<T>`
- Use `Optional` for nullable return values
- Use `var` when type is obvious

## Architecture (Hexagonal)

```
src/main/java/aros/services/rms/
├── core/
│   ├── common/logger/           # Logger interface (no Spring)
│   └── {domain}/
│       ├── application/usecases/ # Business logic
│       ├── application/exception/
│       ├── domain/              # Entities, Value Objects
│       └── port/               # Input/Output interfaces
└── infraestructure/
    ├── {module}/api/            # Controllers, DTOs
    ├── {module}/persistence/    # JPA entities, Repositories
    └── common/                  # Global config
```

## Dependency Rules

**CRITICAL: Core must NOT depend on Infrastructure**

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
- Use case implementations
- JPA repositories
- Controllers
- Logger implementation
- GlobalExceptionHandler

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

## Environment Variables

### Development (.env)
```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=rmsdb
DB_USERNAME=root
DB_PASSWORD=your_password
```

### Production (Cloud Run)
```bash
SPRING_PROFILES_ACTIVE=prod
CLOUD_SQL_INSTANCE=project:region:instance
DB_NAME=rmsdb
DB_USERNAME=iam_user
# No DB_PASSWORD needed (IAM authentication)
```

## Spring Profiles

| Profile | File | Use Case |
|---------|------|----------|
| default | application.yml | Common config |
| dev | application-dev.yml | Local development (TCP/IP) |
| prod | application-prod.yml | Cloud Run with IAM |

**Note**: Profile files override base configuration. Use `SPRING_PROFILES_ACTIVE=prod` in production.

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

### Log Levels
- **ERROR**: Exceptions, business failures
- **INFO**: Business actions (create, update, state changes)
- **DEBUG**: Validation details, input data

### Message Format
```
Action: entity=id, detail={}
Example: "Order cancelled: id=123, reason=client_request"
```

## Swagger / OpenAPI

### Rules
- Every `@RestController` needs `@Tag`
- Every endpoint needs `@Operation` with summary and description
- Use `@ApiResponse` to document possible response codes
- Group endpoints by domain using tags

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

## Dependency Management

- DO NOT change dependency versions without research
- Check [Maven Repository](https://mvnrepository.com/) for GA versions
- Verify compatibility with Spring Boot and Java 21
- Test locally before committing

## Common Errors

- **Port in use**: Check for running instances
- **DB connection failed**: Ensure Docker containers are running
- **Test failures**: Verify database is accessible and configured

## Useful Commands

```bash
./gradlew dependencies   # Check dependencies
./gradlew projects      # Show project structure
./gradlew properties    # Show properties
./gradlew generate-jwt-keys  # Generate JWT key pair
```
