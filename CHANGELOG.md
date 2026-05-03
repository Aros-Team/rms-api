# Changelog

## [v0.3.7](https://github.com/Aros-Team/rms-api/releases/tag/v0.3.7)

### Bug Fixes

- replace text blocks with escaped JSON strings to fix NPE in Checkstyle 13.4.1

## [v0.3.7]

### Features

- Order and inventory monitoring
- User status management with update, delete, and retry-email endpoints
- Local mail service with amail for development
- Database Audit logs
- Inventory transfer endpoint
- Migration with comprehensive seed data
- WebSocket support
- Add update, soft-delete support with deletedAt column, and status persistence for users

### Bug Fixes

- User creation with welcome token flow
- Add @Enumerated(EnumType.STRING) to UserEntity role and status fields
- Bug in Create new order
- Improve configuration validation and security for production

### Miscellaneous

- JWT key generation and parsing - PEM format with proper headers
- Normalize PEM tokens to raw format
- Resolved format for appProperties
- Removed bad seed data file and restructured V8 migrations
- Update profile configurations for prod and dev

## [v0.3.0]

### Features

- **prometheus**: Custom business metrics for authentication (login attempts, password reset)
- **prometheus**: Custom business metrics for orders (created, status transitions, cancelled, delivered)
- **prometheus**: Prometheus metrics endpoint exposed at `/metrics`
- **inventory**: Supply management infrastructure
- **inventory**: Purchase order workflow

### Bug Fixes

- Product options relationship in orders
- Password validation strength
- Dependencies and build issues
- CurrentUser and Supplier dependencies
- Database migration issues

### Miscellaneous

- Prometheus endpoint moved to separate management port
- CORS origins configurable via environment
- Profile configuration optimized

## [v0.2.1]

### Miscellaneous

- Security configuration updates

## [v0.2.0]

### Features

- **order**: Complete order lifecycle: QUEUE → PREPARING → READY → DELIVERED (+ CANCELLED)
- **order**: Order details with product options
- **order**: Multi-area order preparation tracking

## [v0.1.0]

### Features

- **auth**: JWT-based authentication with access and refresh tokens
- **auth**: Password reset flow with secure tokens
- **auth**: Two-Factor Authentication (2FA) support
- **auth**: Role-based access control (ADMIN, WORKER)
- **auth**: Device management for persistent sessions

- **domain**: Area: Preparation areas management (Bar, Kitchen, Grill)
- **domain**: Category: Product categorization
- **domain**: Table: Table availability and status management
- **domain**: Product: Products with optional variants
- **domain**: ProductOption: Configurable options per product
- **domain**: Device: Device tracking for auth

- **infra**: Custom exceptions in core layer
- **infra**: GlobalExceptionHandler (400, 401, 404, 409, 500, 503)
- **infra**: Logger interface in core with SLF4J implementation
- **infra**: Swagger/OpenAPI with springdoc 3.0.2
- **infra**: Request logging filter
- **infra**: JWT configuration validator
- **infra**: Flyway migrations
- **infra**: Docker support with multi-stage Dockerfile

- **testing**: Unit tests for all use cases (Area, Category, Table, Product, Order)

- **build**: Spring Boot 4.0.3 with Java 21
- **build**: Gradle configuration with version from APP_VERSION env var

## [v0.0.1]

### Miscellaneous

- Initial project setup