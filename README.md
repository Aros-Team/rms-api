# Restaurant Management System (RMS)

**API repository**
**Version:** 0.1.0

Robust, scalable API that processes restaurant operations in real-time and transforms data into actionable business insights.


## Context

Our first project, ROS, focused exclusively on order management. While it worked well for that specific task, we realized restaurants needed more—they needed a system that understood their entire business.

This new platform represents our evolution: a comprehensive Restaurant Management System built around real business needs. We're not just tracking orders anymore; we're helping managers make smarter decisions with data-driven insights that drive actual growth.

## Technologies We're Using

The Aros system is built on a modern and scalable stack, optimized for cloud-native performance and security:

- **Core Framework:**  Spring Boot 4.0.3 running on Java 21, leveraging the latest performance enhancements and virtual threads for high concurrency.

- **Data Persistence:** MySQL 7.4, managed via Spring Data JPA to ensure relational integrity, ACID compliance, and efficient transaction handling.

- **Gradle:** As the build automation tool and dependency manager.

- **Docker:** For application containerization, ensuring consistency across development and production environments.


## Requirements

Before running this project, ensure you have installed:

- [Docker Engine](https://docs.docker.com/engine) – required for building and running the application
- [Taskfile](https://taskfile.dev/docs/installation) – to simplify command execution


## First steps

- **Run the project:**
  ```
  task run
  ```

- **Build the project:**
  ```
  # This command runs tests and then generates a Docker image
  task build
  ```

- **Format the code:**
  ```
  task format
  ```
