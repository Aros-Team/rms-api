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

# Format code (check if available)
task format

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

# Run tests in continuous mode (watch for changes)
./.engine/gradlew test --continuous

# Run a specific test task with debug logging
./.engine/gradlew test --debug
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
docker compose up -d db

# Stop all containers
docker compose down

# View logs
docker compose logs -f
```

## Code Style Guidelines

### General Principles
- Use **4 spaces** for indentation (no tabs)
- Follow **Spring Boot conventions** and Java 21 best practices
- Keep methods short and focused (single responsibility)
- Write meaningful commit messages

### Naming Conventions
- **Classes**: PascalCase (e.g., `OrderService`, `MenuItemController`)
- **Methods**: camelCase (e.g., `findById`, `processOrder`)
- **Variables**: camelCase (e.g., `orderList`, `maxItems`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- **Packages**: lowercase with dots (e.g., `aros.services.rms.controller`)

### Package Structure
```
src/main/java/aros/services/rms/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── service/          # Business logic
├── repository/       # Data access (JPA)
├── model/            # Entity classes
├── dto/              # Data Transfer Objects
├── exception/        # Custom exceptions
└── security/         # Security configurations
```

### Imports
- Use explicit imports (no wildcard `.*` except for static imports)
- Order imports: java > javax > org > com > all other packages
- Group related imports together

### Types and Generics
- Use generics for collections: `List<Order>` not `List`
- Prefer interfaces over concrete types: `List<T>` not `ArrayList<T>`
- Use `Optional` for nullable return values
- Use `var` for local variable type inference when type is obvious

### Error Handling
- Use custom exceptions with `@ResponseStatus` or `@ControllerAdvice`
- Return appropriate HTTP status codes (200, 201, 400, 404, 500)
- Log errors with appropriate levels (ERROR for failures, DEBUG for details)
- Never expose stack traces to clients

### Lombok Usage
- Use `@Data` for simple DTOs/entities (generates getters/setters/equals/hashCode/toString)
- Use `@Builder` for complex objects with many fields
- Use `@Slf4j` for logging
- Use `@AllArgsConstructor` and `@NoArgsConstructor` as needed

### Testing
- Use `@SpringBootTest` for integration tests
- Use `@DataJpaTest` for repository tests
- Use `@WebMvcTest` for controller tests
- Use `@MockBean` to
- Follow mock dependencies naming: `ClassNameTests` for test class

### Database/JPA
- Use meaningful entity names with `@Entity` and `@Table`
- Define relationships with `@OneToMany`, `@ManyToOne`, etc.
- Use repository interfaces extending `JpaRepository`
- Prefer JPQL queries over native queries when possible

### Security
- Use Spring Security for authentication/authorization
- Never hardcode secrets; use environment variables
- Validate all inputs with `@Valid` and Bean Validation annotations

### Documentation
- Use Javadoc for public APIs
- Document complex business logic with inline comments
- Keep README.md updated with setup instructions

## Environment Variables

Create a `.env` file (see `.env.example`):
```bash
DB_ROOT_PASSWORD=your_password
DB_NAME=rms
DB_PORT=3306
```

## Database Configuration

The application expects MySQL to be available at `localhost:3306`. Use Docker Compose to start the database:
```bash
docker compose up -d db
```

## Common Issues

- **Port already in use**: Check if another instance is running or change port in `application.properties`
- **Database connection failed**: Ensure Docker containers are running
- **Test failures**: Check database is accessible and properly configured

## Useful Gradle Commands

```bash
# Check dependencies
./.engine/gradlew dependencies

# Show project structure
./.engine/gradlew projects

# Show properties
./.engine/gradlew properties

# Update Gradle wrapper
./.engine/gradlew wrapper --gradle-version=9.x
```
