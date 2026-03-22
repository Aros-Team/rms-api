# RMS Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v0.2.0] - 2026-03-20

### Added

- **Order Management**:
  - Complete order lifecycle: QUEUE → PREPARING → READY → DELIVERED (+ CANCELLED)
  - Order details with product options
  - Multi-area order preparation tracking

## [v0.1.0] - 2026-03-17y

### added

- **Authentication & Authorization**:
  - JWT-based authentication with access and refresh tokens
  - Password reset flow with secure tokens
  - Two-Factor Authentication (2FA) support
  - Role-based access control (ADMIN, WORKER)
  - Device management for persistent sessions

- **Domain Modules** (Hexagonal Architecture):
  - **Area**: Preparation areas management (Bar, Kitchen, Grill)
  - **Category**: Product categorization
  - **Table**: Table availability and status management
  - **Product**: Products with optional variants
  - **ProductOption**: Configurable options per product
  - **Device**: Device tracking for auth

- **Infrastructure**:
  - Custom exceptions in core layer
  - GlobalExceptionHandler (400, 401, 404, 409, 500, 503)
  - Logger interface in core with SLF4J implementation
  - Swagger/OpenAPI with springdoc 3.0.2
  - Request logging filter
  - JWT configuration validator
  - Flyway migrations
  - Docker support with multi-stage Dockerfile

- **Testing**:
  - Unit tests for all use cases (Area, Category, Table, Product, Order)

- **Build**:
  - Spring Boot 4.0.3 with Java 21
  - Gradle configuration with version from APP_VERSION env var

## [v0.0.1] - 2026-02-24

### added

- Initial project setup
