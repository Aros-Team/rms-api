# AGENTS.md — AI Agent Navigation Map

> Entry point for agent work in this repo. NOT bible: **map**. Read only what you need when you need it (progressive disclosure).


## 1. Before Start (mandatory)

1. Run `./harness/harness.sh` — verify exit 0 and `[OK]` on all sections. If it fails, **STOP** — resolve env before touching code.
2. If harness reports missing files (`AGENTS.md`, `activities.json`, `.opencode/agent/`, `progress/`, `docs/`), run `./harness/build-harness.sh` to create them.
3. Read `progress/current.md` — understand state from last session.
4. Read `activities.json` — identify pending activities and their tasks.
5. Read `.opencode/agent/` — load the role definitions for the agents you will use.
6. Assign ONE task from ONE pending activity to a sub-agent via delegation. **Never work directly — always delegate.**

## 2. Repo Map

**Workspace root:** `/home/jorge/Projects/aros/rms/api`

| File / folder | Contains | When read |
|---|---|---|
| `activities.json` | Activity list (pending/in_progress/done) | Always, at start |
| `.opencode/agent/main-orchestrator.md` | Primary agent definition (coordinator) | Always, at start |
| `.opencode/agent/task-executor.md` | Implementer subagent | Before delegating implementation |
| `.opencode/agent/implementation-reviewer.md` | Reviewer subagent | Before marking `done` |
| `.opencode/package.json` | `@opencode-ai/plugin` dependency | Setup only |
| `progress/current.md` | Current session state | Always, at start |
| `progress/history.md` | Append-only log of previous sessions | Need historical context |
| `docs/architecture.md` | Hexagonal layering, use case naming, exceptions | Before implement |
| `docs/conventions.md` | Style rules, names, Lombok usage, Checkstyle traps | Before write code |
| `docs/verification.md` | How verify work works (the 8 harness sections) | Before declare task `done` |
| `docs/CHECKPOINTS.md` | Final state evaluation checklist | Before declare task `done` |
| `harness/harness.sh` / `.bat` | Entry verifier (env + quality + tests) | Before start |
| `harness/build-harness.sh` / `.bat` | Creates `activities.json` + `progress/` + `docs/` | For setup |
| `src/` | Application code (hexagonal: `domain/`, `application/`, `infrastructure/`) | For implement |
| `build.gradle` | Gradle build (plugins, deps, Checkstyle, Spotless, JWT task) | Need build context |
| `Taskfile.yml` | Convenience entry (`task run`, `task test`, ...) | Optional — humans use this |
| `compose.yml` + `Dockerfile` | Local DB + mail stack and prod image | Need container context |
| `gradlew` / `gradlew.bat` | Gradle wrapper (called by harness and humans) | Already wired; do not edit |
| `.env` / `.env.example` | Local config (DB, mail, JWT keys) | Setup only — never commit `.env` |

### 2.1 Path Rules

- **All paths relative to workspace root.** The workspace root is where `AGENTS.md` lives.
- **NEVER use absolute paths** in commits, configs, or JSON.
- **NEVER construct paths** with `../` to go above workspace root.

## 3. Hard Rules (non-negotiable)

- **One task at a time.** Do NOT mix tasks from different activities. Tasks can run in parallel only if from the same activity and different agents.
- **Do NOT declare task done without green harness.** Run `./harness/harness.sh` — all 8 sections must be `[OK]`.
- **Document what you do** in `progress/current.md` while working, NOT at the end.
- While an activity is in progress, write the implementation plan and task breakdown in `progress/current.md`.
- **Leave repo clean** before closing session (see §6).
- **If you don't know something, search `docs/`** before making it up.
- **Core must NOT depend on Infrastructure.** No `org.springframework.*` or `jakarta.persistence.*` imports in `domain/`. Enforced by harness §6.
- **Never use build commands directly to "fix" things** — that's what `./gradlew spotlessApply` and `./gradlew test` are for. Don't edit generated sources.
- **Never commit `.env`, `build/`, `.gradle/`, `*.log`, `bin/`, `uploads/`, generated JWT keys.**

## 4. Activity Types

- `fix` — Bug fix
- `feat` — New feature
- `chore` — Maintenance task (refactor, deps, config)

Task agents (declared in `.opencode/agent/`):
- `implementer` → `task-executor.md`
- `reviewer` → `implementation-reviewer.md`

The orchestrator (this file's reader) → `main-orchestrator.md`.

## 5. Available Agents for Delegation

Use the `delegate` tool to assign tasks. Available agents:

| Agent | Use when |
|-------|----------|
| `task-executor` | Leader assigns specific implementation tasks |
| `implementation-reviewer` | Validate completed work before marking done |
| `explore` | Fast codebase exploration (find files, search patterns) |
| `general` | Research complex questions, multi-step tasks |

> Agent definitions live in `.opencode/agent/*.md`. Read them before delegating — they spell out capabilities (edit / write / bash / delegate) and hard rules.

### 5.1 How to Delegate

1. Open `activities.json`
2. Filter activities with `status == "pending"` or `"in_progress"`
3. For each pending activity, check its `tasks` array
4. Pick ONE task with `status: pending`
5. Delegate to the appropriate agent (`task-executor` for implement, `implementation-reviewer` for verify)
6. Update task `status: in_progress` in `activities.json`
7. Annotate `progress/current.md`: activity, task, start time

> An activity can have multiple pending tasks delegated in parallel to different agents.

## 6. Session Close (lifecycle)

Before end:

1. Run `./harness/harness.sh` — all green.
2. If task done: mark task `status: done` in `activities.json`.
3. If all tasks in activity are `done`: mark activity `status: done` in `activities.json`.
4. Once an activity and ALL its tasks are done: move summary from `progress/current.md` to end of `progress/history.md`.
5. Then empty `progress/current.md` leaving only the template (`# Current Session` header + field stubs).
6. Do NOT leave temp files, `System.out.println` debug, or TODOs without context.

### 6.1 All Activities Complete

When ALL activities in `activities.json` are `status: done`:

1. Ask the user: "Do you want to clean the session to start another one?"
2. If **yes**:
   - Replace `activities.json` content with `[]`.
   - Empty `progress/current.md` to just the template.
   - Optionally save final state to `progress/history.md`.
3. If **no**:
   - Keep `activities.json` as is for reference.
   - End session normally.

## 7. Greeting Message

When the leader agent (orchestrator) starts a new session, send this initial message:

```
| > Hello, I am the main orchestrator for the RMS API.

My role is to coordinate implementation work and make sure everything advances in an orderly way.

We currently have X pending activity(ies) in the queue.

What would you like to do today with the RMS API project?
- Implement a new feature?
- Fix a bug?
- Make an improvement or refactor?
```

Replace `X` with the actual count of pending activities from `activities.json`.

## 8. If Blocked

- Re-read relevant section in `docs/`.
- Re-read the relevant agent definition in `.opencode/agent/`.
- If a tool doesn't do what you expect, **do NOT invent a workaround**: document the block in `progress/current.md` and stop the session.

---

## Appendix A — Harness quick reference

```bash
# Verify environment + run quality + tests
./harness/harness.sh

# Bootstrap harness files (idempotent)
./harness/build-harness.sh

# Equivalent on Windows
harness\harness.bat
harness\build-harness.bat
```

The harness mirrors `frontend/scripts/harness.js` but adapted: java/gradle/docker checks instead of node/npm, and `./gradlew spotlessCheck + checkstyleMain + checkstyleTest` instead of `npm run lint`. Section 6 is rewritten to enforce hexagonal layering instead of `DESIGN.md` style rules.

The agent registry lives in `.opencode/agent/*.md` — same shape as the frontend.

Each Taskfile task (`run`, `build`, `test`, `format`, `clean`, `jwtkeys`) keeps its entry in `Taskfile.yml`; the harness does NOT duplicate those commands. Humans use `task <name>` or the equivalent `gradlew` invocation.
