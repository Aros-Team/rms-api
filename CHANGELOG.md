# Changelog

## [v2.5.0](https://github.com/Aros-Team/rms-api/releases/tag/v2.5.0)

### Features

- **security**: enforce role and area authorization across the API
- **seed**: deterministic 180-day dataset, reset CLI and dev endpoint
- **auth**: log the 2FA code at DEBUG in development
- **analytics**: operations, cohort, alerts and real top-selling
- **payments**: minimal charge-and-close and cash shifts
- **orders**: add GET /orders/{id}
- **seed**: cohort identity, alerts, payments and reset safety
- **settings**: typed business, currency, labor and notification settings
- **payroll**: aggregate events and settlements into the summary
- **seed**: suppliers, purchases and a reconciled stock ledger
- **schedule**: clock in/out, withinShift and assignment rules
- **kitchen**: station fields, station filter and item readiness
- **backend**: typed purchase status, payroll transactions and money precision
- **settings**: add weekly opening hours
- **seed**: rotate shifts under 42 hours and use realistic names
- **notifications**: persist and broadcast admin notifications
- **seed**: bundle square product photos and add a seedReset task
- **schedules**: notify clock-in and 10-minute late arrivals
- **websocket**: expose a native /ws-native STOMP endpoint
- **product**: expose responsive image variants in product responses

### Bug Fixes

- **time**: pin Hibernate to the business timezone
- **twofactor**: return the newest valid code
- **analytics**: accept LocalDateTime from the cohort native query
- **analytics**: use AnalyticsPeriods for menu-engineering keys
- **products**: preserve options and recipes when update omits them
- **orders**: reject unsupported sort fields with 400
- **schedule**: add OUT to LogType
- **seed**: include manual payroll baseline and seeded events in totals
- **api**: contract statuses, pagination and batch fetching
- **seed**: guarantee 80+ current-day orders without future times

## [v2.4.0](https://github.com/Aros-Team/rms-api/releases/tag/v2.4.0)

### Features

- **payroll**: payroll table, system_configuration, AreaLaborCostPort, and labor cost stabilization
- **payroll**: events, settlements & aggregation + labor cost stabilization

### Bug Fixes

- **data.sql**: correct payroll seed column name registered_by
- **data.sql**: delete payroll before users to satisfy FK constraint
- **system-config**: manage group createdAt in entity lifecycle
- **contract**: align API responses with the frontend contract

## [v2.3.0](https://github.com/Aros-Team/rms-api/releases/tag/v2.3.0)

### Features

- **supplies**: paginate supply variants get-all endpoint
- **product**: add PUT /api/v1/products/{id}/enable endpoint
- **manage**: add ?search= query param to 10 manage list endpoints
- **options**: cost projection + selection modes + substitution/REMOVE
- **option-group**: Product-OptionGroup M:N association with required flag

### Bug Fixes

- **supplies**: return unitCost in supply variant GET response
- **products**: resolve primary image URL in findAll via batch query

## [v2.2.0](https://github.com/Aros-Team/rms-api/releases/tag/v2.2.0)

### Features

- fix 8 frontend-blocking combo API gaps
- money value object + public JWKS endpoint
- **analytics**: API contract + pre-req schema (V32 migration, analytics config endpoints, order timestamps)
- **analytics**: prime cost & margins module (V33+V34, aggregation, GET endpoint)
- **analytics**: menu engineering BCG module (V35, quadrant analysis, cache, invalidation events)
- **orders**: add paginated history endpoint with filtering

### Bug Fixes

- **menu-engineering**: review fixes — extract JPA to infra adapter, add event handler tests, remove dead exception
- **test**: make JwksControllerSecurityIntegrationTest work without MySQL (use H2 + @MockitoBean AdminInitializer)

## [v2.1.0](https://github.com/Aros-Team/rms-api/releases/tag/v2.1.0)

### Features

- add stimated product cost

## [v2.0.0](https://github.com/Aros-Team/rms-api/releases/tag/v2.0.0)

### Features

- add WebSocket topic for cancelled orders notifications
- notify table status changes from orders and admin actions
- **BREAKING**: add product image management with local storage support
- add employees endpoint, product pagination with inactive filter, and supplies pagination
- salary history
- workers schedule
- **special-selection**: full Special Selection (combo) module replacing day menu

### Bug Fixes

- error message for area when create/update user
- error for area on user creation/update
- salary history test and user test modification
- code quality
- repair V8 seed data area and inventory storage locations
- auth error response crashes with HttpMediaTypeNotAcceptableException
- PageImpl serialization warning - use PagedModel VIA_DTO
- PageImpl serialization warning
- expand allowed password symbols to all ASCII, centralize validation
- test are possible

## [v1.0.0](https://github.com/Aros-Team/rms-api/releases/tag/v1.0.0)

### Bug Fixes

- **BREAKING**: added "areas" attribute on user creation and modification
- remove spotless rule to allow break lines
- **BREAKING**: areas on user creation/modification

## [v0.4.0](https://github.com/Aros-Team/rms-api/releases/tag/v0.4.0)

### Features

- **service**: fetch and populate product recipe in findById
- **inventory**: implement WebSockets and new order delivery topic
- **db**: enhance V8 seed data with customizable menu items

### Bug Fixes

- return proper 401 error when credentials are invalid instead of 500
- add character limits and name pattern validation to user DTOs
- improve name validation error message to be more specific
- remove password from auth endpoint response

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