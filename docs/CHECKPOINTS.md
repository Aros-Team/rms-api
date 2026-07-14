# Checkpoints

> Final state evaluation before closing a session or declaring an activity done. Walk this list top to bottom.

## Per-task close checklist

- [ ] `./harness/harness.sh` exits 0
- [ ] No new `WARN` lines that weren't there before (regressions in warnings count)
- [ ] No `TODO`, `FIXME`, or `System.out.println` left behind
- [ ] No commented-out code blocks larger than 2 lines
- [ ] No accidental file modifications (`git status` is scoped)
- [ ] No hardcoded secrets, URLs, or credentials in the diff
- [ ] `src/main/resources/db/migration/` has a new migration if the schema changed
- [ ] New public methods have Javadoc explaining intent
- [ ] New endpoints have `@Tag`, `@Operation`, and `@ApiResponse` annotations
- [ ] DTOs use `@NotNull` / `@NotBlank` / `@Size` where appropriate
- [ ] Tests cover: happy path, invalid input, missing entity (where applicable)
- [ ] `progress/current.md` reflects what was actually done

## Per-activity close checklist

- [ ] Every task in the activity is `status: done`
- [ ] The activity itself is flipped to `status: done` in `activities.json`
- [ ] Summary moved from `progress/current.md` → end of `progress/history.md`
- [ ] `progress/current.md` reduced to the template (only the `# Current Session` header and the field stubs)
- [ ] Branch is ready to push / PR

## Session close checklist

- [ ] `./harness/harness.sh` is green
- [ ] No uncommitted `build/`, `.gradle/`, `*.log`, `bin/` artifacts
- [ ] No leftover `*.bak`, `*.tmp`, or scratch files
- [ ] `progress/current.md` either empty (template only) or moved to history
- [ ] All open `activities.json` entries have a clear next-action

## When ALL activities are done

When every entry in `activities.json` is `status: done`:

1. Ask the user: "Do you want to clean the session to start another one?"
2. If **yes**:
   - Replace `activities.json` content with `[]`
   - Reset `progress/current.md` to the template
   - Optionally append a final summary to `progress/history.md`
3. If **no**:
   - Leave `activities.json` as-is for reference
   - End the session normally

## Red flags that mean "do not close yet"

- ⚠️ Harness warnings increased from last session
- ⚠️ `git diff` shows modifications outside the task's scope
- ⚠️ A task was marked `done` without `./harness/harness.sh` being green at the time
- ⚠️ A Flyway migration references a column that doesn't exist yet
- ⚠️ A test was `@Disabled` or commented out
- ⚠️ `progress/current.md` describes work not present in `git diff`

If any red flag is true → return to the task, fix it, then close.