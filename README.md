# Restaurant Management System (RMS)

**API repository**

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

- **API Documentation:**
  Access Swagger UI at: http://localhost:8080/swagger-ui/index.html


## AI Agent Harness

This repo ships an AI-agent harness mirroring `rms/frontend/scripts/harness.js`, ported to **`.sh` + `.bat`**. Read [`AGENTS.md`](./AGENTS.md) before starting any agent-driven work.

The agent registry lives at [`.opencode/agent/`](./.opencode/agent/):

| Agent | File | Role |
|-------|------|------|
| `main-orchestrator` | `main-orchestrator.md` | Coordinates delegation |
| `task-executor` | `task-executor.md` | Implements tasks |
| `implementation-reviewer` | `implementation-reviewer.md` | Validates before `done` |

Activities (the units of work delegated to agents) live in [`activities.json`](./activities.json) — top-level array, schema documented in `.opencode/agent/main-orchestrator.md`.

```bash
# Verify environment + code quality + tests (cross-platform)
./harness/harness.sh        # Linux / macOS / Git Bash
harness\harness.bat         # Windows

# Bootstrap harness files on a fresh clone (idempotent)
./harness/build-harness.sh
```

A task is only `done` when `./harness/harness.sh` exits `0` and all eight sections print `[OK]`. See [`docs/verification.md`](./docs/verification.md) and [`docs/CHECKPOINTS.md`](./docs/CHECKPOINTS.md) for the full close protocol.

