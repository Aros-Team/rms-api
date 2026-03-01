# AGENTS.md - Agentic Coding Guidelines

This document provides guidelines for agents working on the RMS (Restaurant Management System) API codebase.

## Project Overview

- **Framework**: Spring Boot 4.0.3 with Java 21
- **Build Tool**: Gradle 9.3.x
- **Database**: MySQL 7.4
- **Key Dependencies**: Spring Data JPA, Spring Security, Spring Validation, Spring WebMVC, Spring WebSocket, Lombok

## Build Commands

### Standard Commands
```bash
# Run the application (starts Docker containers + Spring Boot)
task run

# Build project and generate Docker image
task build

# Clean build directory
./.engine/gradlew clean

# Build without tests
./.engine/gradlew assemble
```

### Testing Commands
```bash
# Run all tests
./.engine/gradlew test

# Run a single test class
./.engine/gradlew test --tests "aros.services.rms.RmsApplicationTests"

# Run a specific test method
./.engine/gradlew test --tests "aros.services.rms.RmsApplicationTests.contextLoads"

# Run tests with verbose output
./.engine/gradlew test --info
```

### Running the Application
```bash
# Run Spring Boot application (requires Docker containers)
./.engine/gradlew bootRun

# Build and run JAR
./.engine/gradlew bootJar
java -jar build/libs/rms-0.1.0.jar
```

### Docker Commands
```bash
# Start only the database
docker compose up -d

# Stop all containers
docker compose down
```

## Code Style Guidelines

### General Principles
- Use **Google style guide**
- Follow **Spring Boot conventions** and Java 21 best practices
- Keep methods short and focused (single responsibility)
- Write meaningful commit messages

### Naming Conventions
- **Classes**: PascalCase (e.g., `OrderService`, `MenuItemController`)
- **Methods**: camelCase (e.g., `findById`, `processOrder`)
- **Variables**: camelCase (e.g., `orderList`, `maxItems`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- **Packages**: lowercase with dots (e.g., `aros.services.rms.controller`)

### Package Structure (Hexagonal Architecture)
```
src/main/java/aros/services/rms/
├── core/
│   └── {domain}/           # Domain entities
│      ├── application/    # Use cases & exceptions
│      ├── domain/         # Domain models
│      └── port/         # Input/Output interfaces
└──infraestructure/
       ├── {module}/       # API DTOs, JPA repos, configs
       └── common/         # Shared config, CORS, logging
```

### Imports
- Use explicit imports (no wildcard `.*` except for static imports)
- Order: java > javax > org > com > all other packages

### Types and Generics
- Use generics for collections: `List<Order>` not `List`
- Prefer interfaces: `List<T>` not `ArrayList<T>`
- Use `Optional` for nullable return values
- Use `var` when type is obvious

### Error Handling
- Use custom exceptions with `@ResponseStatus` or `@ControllerAdvice`
- Return appropriate HTTP status codes (200, 201, 400, 404, 500)
- Log errors with appropriate levels (ERROR for failures, DEBUG for details)
- Never expose stack traces to clients

### Lombok Usage
- Use `@Data` for simple DTOs/entities
- Use `@Builder` for complex objects
- Use `@Slf4j` for logging
- Use `@AllArgsConstructor` and `@NoArgsConstructor` as needed

### Testing
- Use `@MockBean` for mocking dependencies
- Name test classes: `ClassNameTests`

### Database/JPA
- Use `@Entity` and `@Table` for entities
- Define relationships with `@OneToMany`, `@ManyToOne`, etc.
- Use repository interfaces extending `JpaRepository`
- Prefer JPQL over native queries when possible

### Security
- Use Spring Security for authentication/authorization
- Never hardcode secrets; use environment variables
- Validate all inputs with `@Valid` and Bean Validation

## Environment Variables

Create a `.env` file (see `.env.example`):
```bash
DB_ROOT_PASSWORD=your_password
DB_NAME=rms
DB_PORT=3306
```

## Database Configuration

The application expects MySQL at `localhost:3306`. Start with:
```bash
docker compose up -d
```

## Common Issues

- **Port already in use**: Check if another instance is running
- **Database connection failed**: Ensure Docker containers are running
- **Test failures**: Check database is accessible and properly configured

## Useful Gradle Commands

```bash
./.engine/gradlew dependencies   # Check dependencies
./.engine/gradlew projects       # Show project structure
./.engine/gradlew properties     # Show properties
```
