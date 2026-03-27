# AGENTS.md - Agentic Coding Guidelines

This document provides guidelines for agents working on the RMS (Restaurant Management System) API codebase.

## Project Overview

- **Framework**: Spring Boot 4.0.3 with Java 21
- **Build Tool**: Gradle 9.3.x
- **Database**: MySQL 7.4
- **Key Dependencies**: Spring Data JPA, Spring Security, Spring Validation, Spring WebMVC, Spring WebSocket, Lombok

## Importante: Usar siempre Taskfile

**Todos los comandos deben ejecutarse desde el Taskfile**. No usar `./gradlew` directamente excepto donde sea necesario.

```bash
# Desarrollo - iniciar la aplicación
task run

# Desarrollo - formatear código después de hacer cambios
task format

# Build con Docker
task build

# Tests
task test

# Limpiar
task clean
```

## Build Commands

### Standard Commands
```bash
# Run the application (starts Docker containers + Spring Boot)
task run

# Build project and generate Docker image
task build

# Clean build directory
./gradlew clean

# Build without tests
./gradlew assemble
```

### Testing Commands
```bash
# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "aros.services.rms.RmsApplicationTests"

# Run a specific test method
./gradlew test --tests "aros.services.rms.RmsApplicationTests.contextLoads"

# Run tests with verbose output
./gradlew test --info
```

### Running the Application
```bash
# Run Spring Boot application (requires Docker containers)
./gradlew bootRun

# Build and run JAR
./gradlew bootJar
java -jar build/libs/rms-0.1.0.jar
```

### Docker Commands
```bash
# Start only the database
docker compose up -d

# Stop all containers
docker compose down

# View container logs
docker compose logs -f

# Rebuild containers (after code changes)
docker compose up -d --build

# Stop and remove containers, networks, and volumes
docker compose down -v
```

## Docker Best Practices

### Image Building
- Usar **multi-stage builds** para imágenes más pequeñas y seguras
- Minimizar el número de capas: agrupar instrucciones similares (`RUN`, `COPY`)
- Orden de instrucciones: lo que cambia menos primero (dependencias antes que código)
- Usar `.dockerignore` para excluir archivos innecesarios del contexto de build
- No instalar herramientas de desarrollo en la imagen final

### Multi-Stage Build Example
```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew assemble

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Image Layers
- Cada instrucción en un Dockerfile crea una nueva capa
- Capas que cambian frecuentemente (código fuente) deben ir al final
- Capas estáticas (dependencias, JDK) deben ir al principio
- Usar `docker history` para inspeccionar las capas de una imagen

### Build Cache
- El orden de instrucciones afecta el cache: poner lo que cambia menos primero
- Invalidar cache: `COPY`, `RUN` con dependencias cambiadas
- Saltar cache con `--no-cache` solo cuando sea necesario

### Container Security
- No ejecutar como root: usar `USER` instruction
- Usar imágenes oficiales y verificadas (eclipse-temurin, postgres)
- No exponer credenciales en variables de entorno (usar secrets)
- Imágenes distroless para producción
- Escanear imágenes con `docker scout` o `trivy`

### Docker Compose

#### Estructura Recomendada
```yaml
services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=db
    depends_on:
      db:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  db:
    image: mysql:7.4
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME}
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql_data:
```

#### Mejores Prácticas
- Usar `depends_on` con `condition: service_healthy` para dependencias
- Definir `healthcheck` en todos los servicios
- Usar variables de entorno desde `.env` (nunca hardcodear)
- Usar redes personalizadas para aislar servicios
- Named volumes para persistencia de datos

### Persistencia de Datos
- Usar **named volumes** para datos que deben persistir
- Usar **bind mounts** solo para desarrollo (código fuente)
- No persistir datos en volúmenes temporales
- backup Regular de volúmenes importantes

### Publishing Ports
- Usar formato `"host:container"` para puertos exposés
- Solo exponer puertos necesarios
- En desarrollo: mapear a puertos locales diferentes si hay conflictos

### Recursos de Referencia
- [Dockerfile Best Practices](https://docs.docker.com/get-started/docker-concepts/building-images/writing-a-dockerfile/)
- [Multi-stage Builds](https://docs.docker.com/get-started/docker-concepts/building-images/multi-stage-builds/)
- [Image Layers](https://docs.docker.com/get-started/docker-concepts/building-images/understanding-image-layers/)
- [Using Build Cache](https://docs.docker.com/get-started/docker-concepts/building-images/using-the-build-cache/)
- [Docker Compose Best Practices](https://docs.docker.com/guides/docker-compose/)
- [Containerize an application](https://docs.docker.com/get-started/workshop/02_our_app/)
- [Persist the DB](https://docs.docker.com/get-started/workshop/05_persisting_data/)

## Code Style Guidelines

**Después de cualquier cambio en código Java, ejecutar siempre:**
```bash
task format
```

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
│   ├── common/
│   │   └── logger/           # Logger interface (core agnostic)
│   └── {domain}/
│       ├── application/
│       │   ├── usecases/    # Use case implementations
│       │   └── exception/   # Domain exceptions
│       ├── domain/          # Domain models
│       └── port/            # Input/Output interfaces
└── infraestructure/
    ├── {module}/
    │   ├── api/            # Controllers & DTOs
    │   ├── persistence/    # JPA entities & repositories
    │   └── config/         # Module-specific beans
    └── common/             # Shared config
        ├── exception/      # GlobalExceptionHandler
        ├── logger/         # LoggerImpl
        └── swagger/        # SwaggerConfig
```

### Dependency Rules (Hexagonal Architecture)

**REGLA CRÍTICA: Core NO puede depender de Infrastructure**

- **Core** debe ser completamente agnóstico a cualquier framework (Spring, Hibernate, etc.)
- **Core** define la lógica de negocio pura
- **Infrastructure** es la implementación de los puertos (ports) definidos en core
- Solo `infraestructure/` puede importar dependencias de Spring Boot
- El flujo de dependencias es: `core <- infraestructure` (core no conoce a infraestructure)

**En Core está permitido:**
- Clases de dominio (entities, value objects)
- Puertos de entrada (input ports - use cases)
- Puertos de salida (output ports - interfaces/repository contracts)
- Excepciones de negocio
- Interfaces de Logger (sin implementación)

**En Core PROHIBIDO:**
- Anotaciones Spring (`@Service`, `@Repository`, `@Component`, etc.)
- Importaciones de `org.springframework.*`
- Implementaciones de JPA (`@Entity`, `@Table`, `@Column`, etc.)
- Referencias a infrastructure

**En Infrastructure está permitido:**
- Implementaciones de puertos de core
- Anotaciones Spring
- JPA entities
- Controllers
- Configuración de frameworks

### Use Case Naming Conventions

**Los nombres de use cases deben ser autoexplicativos y específicos:**

| Evitar (vago) | Usar (específico) |
|---------------|-------------------|
| `ProcessOrder` | `TakeOrder`, `CancelOrder`, `CompleteOrder` |
| `ManageTable` | `ReserveTable`, `ReleaseTable`, `UpdateTableStatus` |
| `HandlePayment` | `ProcessPayment`, `RefundPayment` |
| `UserService` | `CreateUser`, `AuthenticateUser`, `UpdateUserProfile` |

**Estructura de nombres:**
- `{Verbo}{Entidad}` para acciones de negocio
- El verbo debe ser específico: `Take`, `Cancel`, `Reserve`, `Process`
- NO usar verbos vagos: `Handle`, `Manage`, `Process`, `Do`

**Ejemplos de buenos nombres:**
- `TakeOrderUseCase`
- `CancelOrderUseCase`
- `ReserveTableUseCase`
- `ProcessPaymentUseCase`
- `CreateMenuItemUseCase`
- `UpdateTableStatusUseCase`

### Imports
- Use explicit imports (no wildcard `.*` except for static imports)
- Order: java > javax > org > com > all other packages

### Types and Generics
- Use generics for collections: `List<Order>` not `List`
- Prefer interfaces: `List<T>` not `ArrayList<T>`
- Use `Optional` for nullable return values
- Use `var` when type is obvious

### Error Handling
- Las excepciones de negocio se definen en `core/` (ver sección Exception Handling)
- El manejo HTTP está en `infraestructure/common/`
- Return appropriate HTTP status codes (200, 201, 400, 404, 500)
- Never expose stack traces to clients

### Lombok Usage
- Use `@Data` for simple DTOs/entities
- Use `@Builder` for complex objects
- Use `@Builder.Default` for fields with initial values
- **Use `@Slf4j` only in infrastructure layer** (never in core)
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

## Exception Handling

### Reglas
- Todas las excepciones de negocio se definen en `core/{domain}/application/exception/`
- Las excepciones son clases simples que extienden `RuntimeException`
- **NO** usar anotaciones Spring (@ResponseStatus) en core
- El manejo HTTP se define en `infraestructure/common/exception/GlobalExceptionHandler.java`

### Nomenclatura
- `{Entity}NotFoundException` - para 404 (ej: `OrderNotFoundException`)
- `{Entity}AlreadyExistsException` - para 409 (ej: `TableAlreadyExistsException`)
- `Invalid{Entity}Exception` - para 400 (ej: `InvalidOrderException`)
- `{Business}Exception` - para errores de negocio genéricos

### Ejemplo
```java
// core/order/application/exception/OrderNotFoundException.java
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order not found: " + id);
    }
}
```

```java
// infraestructure/common/exception/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderNotFound(OrderNotFoundException e) {
        return new ErrorResponse(404, e.getMessage());
    }
}
```

## Logging

### Reglas
- **Core NO usa** `@Slf4j` ni `System.out`
- Usar interfaz `Logger` definida en `core/common/logger/Logger.java`
- Implementación en `infraestructure/common/logger/LoggerImpl.java`
- Inyectar `Logger` en use cases que requieran logging

### Niveles
- **ERROR**: Excepciones, fallos de negocio
- **INFO**: Acciones de negocio (creación, actualización, cambios de estado)
- **DEBUG**: Detalles de validación, datos de entrada

### Nomenclatura de mensajes
- Usar formato: `Acción: entidad=id, detalle={}`
- Ejemplo: `Order cancelled: id=123, reason=client_request`

### Interfaz Logger (core)
```java
// core/common/logger/Logger.java
public interface Logger {
    void info(String message, Object... args);
    void error(String message, Exception exception);
    void error(String message, Exception exception, Object... args);
    void debug(String message, Object... args);
}
```

### Implementación (infra)
```java
// infraestructure/common/logger/LoggerImpl.java
@Slf4j
@Component
public class LoggerImpl implements Logger {
    @Override
    public void info(String message, Object... args) {
        log.info(message, args);
    }
    // ...
}
```

### Uso en Use Cases
```java
public class TakeOrderUseCaseImpl implements TakeOrderUseCase {
    private final Logger logger;
    
    public Order execute(TakeOrderCommand command) {
        // ... lógica
        logger.info("Order created: id={}, tableId={}", order.getId(), table.getId());
    }
}
```

## Swagger / OpenAPI

### Dependencia
```groovy
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2'
```

### Reglas
- Toda clase `@RestController` debe tener `@Tag` con descripción
- Todo endpoint debe tener `@Operation` con summary y description
- Usar `@ApiResponse` para documentar códigos de respuesta posibles
- Agrupar endpoints por dominio usando tags
- Configuración global en `infraestructure/common/swagger/SwaggerConfig.java`

### Ejemplo
```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Gestión del ciclo de vida de órdenes")
public class OrderController {
    
    @Operation(
        summary = "Crear nueva orden", 
        description = "Crea una orden en estado QUEUE. Requiere mesa disponible.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Mesa no encontrada"),
            @ApiResponse(responseCode = "409", description = "Mesa no disponible")
        }
    )
    @PostMapping
    public ResponseEntity<OrderResponse> takeOrder(@Valid @RequestBody TakeOrderRequest request) {
        // ...
    }
}
```

## Controller Advice

### Ubicación
- `infraestructure/common/exception/GlobalExceptionHandler.java`

### Reglas
- Un único `@RestControllerAdvice` para toda la aplicación
- Mapear excepciones de core a códigos HTTP apropiados
- Usar record `ErrorResponse` para estructura consistente de errores
- **NO** exponer stack traces en respuestas

### Códigos HTTP estándar
| Código | Uso |
|--------|-----|
| 400 | Bad Request - datos inválidos |
| 404 | Not Found - entidad no encontrada |
| 409 | Conflict - estado inválido para la operación |
| 500 | Internal Server Error - errores inesperados |

## Concurrency / Race Conditions

### Reglas
- Para operaciones que deben ser atómicas y prevenir race conditions, usar `@Lock` en el repository
- Usar `PESSIMISTIC_WRITE` para operaciones de actualización
- El método debe estar dentro de una transacción (`@Transactional` en adapter)

### Ejemplo
```java
// infraestructure/order/persistence/jpa/OrderRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.date ASC LIMIT 1")
Optional<Order> findFirstByStatusOrderByDateAsc(@Param("status") OrderStatus status);
```

## Errores Comunes

- **Port already in use**: Check if another instance is running
- **Database connection failed**: Ensure Docker containers are running
- **Test failures**: Check database is accessible and properly configured

## Useful Gradle Commands

```bash
./gradlew dependencies   # Check dependencies
./gradlew projects       # Show project structure
./gradlew properties     # Show properties
```

## Gestión de Dependencias

### Reglas Obligatorias
- **NO cambiar versiones de dependencias** sin investigar primero en internet las versiones estables disponibles
- Antes de actualizar una dependencia, buscar en [Maven Repository](https://mvnrepository.com/) las versiones GA (General Availability)
- Verificar compatibilidad con Spring Boot y Java actual
- No asumir que versiones más nuevas son mejores - priorizar estabilidad

### Proceso para Actualizar
1. Investigar versiones estables en Maven Repository
2. Verificar changelog/release notes
3. Probar en entorno local antes de commit
4. Documentar el cambio en commit message

### Dependencias Críticas
- Spring Boot: gestionar desde plugin `id 'org.springframework.boot'`
- springdoc-openapi: debe ser compatible con la versión de Spring Boot
- Lombok: versión gestionada por Spring Boot BOM
