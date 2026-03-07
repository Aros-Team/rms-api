# RMS Changelog

## [0.1.0] - 2026-03-07

### added

- **Order Management System**: Complete order lifecycle implementation with hexagonal architecture
  - Order states: QUEUE → PREPARING → READY → DELIVERED (+ CANCELLED)
  - Table management with availability status
  - Product and product options
  - Category and area domains
  - Unit tests for all use cases

- **Infrastructure Common**:
  - Custom exceptions in core layer (OrderNotFoundException, TableNotAvailableException, etc.)
  - GlobalExceptionHandler for HTTP error mapping (404, 409)
  - Logger interface in core with SLF4J implementation
  - Swagger/OpenAPI documentation with springdoc 3.0.2
  - SecurityConfig to permit all endpoints

- **Build Configuration**:
  - Spring Boot 4.0.3
  - springdoc-openapi-starter-webmvc-ui dependency
  - spring-boot-starter-aop dependency
  - Configuration via environment variables

- **AGENTS.md Updates**:
  - Exception handling guidelines
  - Logging guidelines (core without SLF4J)
  - Swagger documentation rules
  - Dependency management rules

## [0.0.1] - 2026-02-24

### added

- Initial project setup
