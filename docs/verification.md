# Verification

> How to confirm a task is actually done. A task is NOT done when the code compiles. It IS done when evidence proves it works.

## The only command that matters

```bash
./harness/harness.sh
```

All eight sections must print `[OK]`. Anything else = not done. Read the output, don't skim it.

## Per-section meaning

| § | Section | What "OK" means |
|---|---------|-----------------|
| 1 | Environment | `java`, `./gradlew`, `docker`, `docker compose` are all callable |
| 2 | Base files | `AGENTS.md`, `activities.json`, `.opencode/agent/*.md`, `progress/`, `docs/*.md` all exist |
| 3 | `activities.json` schema | Statuses / types / agents all match the allowed enums; max 1 activity `in_progress` |
| 4 | Code quality | `spotlessCheck + checkstyleMain + checkstyleTest` passed |
| 5 | Compilation | `compileJava + compileTestJava` succeeded |
| 6 | Hexagonal layering | `domain/` has no `org.springframework.*` or `jakarta.persistence.*` imports |
| 7 | Tests | `./gradlew test` passed (`build/reports/tests/test/index.html`) |
| 8 | Summary | Exit code 0 |

## What "task done" requires

A task is done when **all** of these hold:

1. `./harness/harness.sh` exits 0.
2. The change is scoped to what the task description says (no drive-by edits).
3. Any new code path has a test. Any new branch has coverage.
4. If you touched the database: a new Flyway migration exists under `src/main/resources/db/migration/`.
5. `progress/current.md` has been updated with the work as it happened (not at the end).
6. The corresponding entry in `activities.json` is flipped to `status: done`.

## What "activity done" requires

An activity is done when **all** of its tasks are `done` per the criteria above. Then flip the activity's status to `done` in `activities.json`.

## Manual smoke tests

Some changes can't be observed by the harness alone. Run the relevant smoke test below before flipping to `done`:

| Change area | Smoke test |
|-------------|------------|
| New REST endpoint | `curl -i http://localhost:8080/<path>` after `task run` |
| Auth / JWT | Mint a token via `task jwtkeys`, set `.env`, log in, hit a protected endpoint |
| DB schema | Inspect with `docker compose exec db mysql -uroot -p` |
| Mail | Trigger an action that sends mail; check the dev mail container logs |
| Flyway migration | `./gradlew flywayInfo` shows `SUCCESS` for the new version |

## Forbidden shortcuts

- ❌ "I'll mark it done and run tests later" — never mark done if the harness is failing.
- ❌ "Spotless complains but it's only formatting" — run `task format` first.
- ❌ "I disabled the test temporarily" — open an activity to re-enable it.
- ❌ "It works on my machine" — `./harness/harness.sh` is the contract; if it fails for CI it fails everywhere.

## When the harness itself is broken

If `./harness/harness.sh` is reporting issues that are clearly bugs in the harness (not your code):

1. Document the bug in `progress/current.md` under "Blockers".
2. Do NOT mark your task done.
3. Open an activity to fix the harness.