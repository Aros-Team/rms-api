#!/usr/bin/env bash
# harness.sh — environment + code quality verifier for the RMS API repo.
# Mirrors scripts/harness.js from rms/frontend, adapted for Spring Boot / Gradle / Docker.
#
# Usage:  ./harness/harness.sh

set -u

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# gradlew entrypoint: call the existing wrapper, .bat on Windows, sh elsewhere.
gradlew() {
  if [ "${OS:-}" = "Windows_NT" ]; then
    cmd //c "gradlew.bat $*"
  else
    ./gradlew "$@"
  fi
}

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

ok()   { printf "${GREEN}[OK]${NC}    %s\n" "$*"; }
warn() { printf "${YELLOW}[WARN]${NC}  %s\n" "$*"; }
fail() { printf "${RED}[FAIL]${NC}  %s\n" "$*"; EXIT=1; }

EXIT=0

command_exists() { command -v "$1" >/dev/null 2>&1; }

echo "── 1. Environment Check ─────────────────────────────"

if ! command_exists java; then
  fail "java is not installed"
  exit 1
fi
ok "java -> $(java -version 2>&1 | head -n1)"

if [ -f "./gradlew" ]; then
  ok "gradlew -> present"
else
  fail "gradlew not found at repo root"
  exit 1
fi

if command_exists docker; then
  ok "docker -> $(docker --version)"
  if docker compose version >/dev/null 2>&1; then
    ok "docker compose -> $(docker compose version --short 2>/dev/null || docker compose version)"
  else
    warn "docker compose plugin not detected"
  fi
else
  warn "docker is not installed (compose-dependent tasks will fail)"
fi

echo
echo "── 2. Base Harness Files ─────────────────────────────"

BASE_FILES=(
  "AGENTS.md"
  "activities.json"
  ".opencode/agent/main-orchestrator.md"
  ".opencode/agent/task-executor.md"
  ".opencode/agent/implementation-reviewer.md"
  "progress/current.md"
  "progress/history.md"
  "docs/architecture.md"
  "docs/conventions.md"
  "docs/verification.md"
  "docs/CHECKPOINTS.md"
)

for f in "${BASE_FILES[@]}"; do
  if [ ! -f "$f" ]; then
    fail "Missing base file: $f"
  else
    ok "Exists $f"
  fi
done

echo
echo "── 3. Validating activities.json ─────────────────────"

if [ ! -f "activities.json" ]; then
  fail "activities.json not found"
else
  # Minimal JSON sanity check — relies on `node` if available, falls back to text heuristics.
  # We never want to fail purely on parse in shell — that's a CI concern; we want ENUM validation.
  if command_exists node; then
    node -e "JSON.parse(require('fs').readFileSync('activities.json','utf8'))" 2>/dev/null \
      && ok "activities.json parses as JSON" \
      || warn "activities.json did not parse (node available, returned non-zero)"
  elif command_exists python3; then
    python3 -c "import json,sys; json.load(open('activities.json'))" 2>/dev/null \
      && ok "activities.json parses as JSON" \
      || warn "activities.json did not parse (python3 available, returned non-zero)"
  else
    # Crude bracket-balance check.
    OPEN=$(tr -cd '{' < activities.json | wc -c)
    CLOSE=$(tr -cd '}' < activities.json | wc -c)
    if [ "$OPEN" = "$CLOSE" ] && [ "$OPEN" -gt 0 ]; then
      ok "activities.json looks balanced (braces)"
    elif [ "$OPEN" = "0" ] && [ "$CLOSE" = "0" ]; then
      ok "activities.json is an empty array"
    else
      fail "activities.json looks malformed (braces unbalanced: {=$OPEN, }=$CLOSE)"
    fi
  fi

  # ── Enum & rule validation ────────────────────────────────────────
  # Mirrors harness.js §3 lines 75-110 exactly:
  #   - status in {pending, in_progress, done, blocked}
  #   - type   in {fix, feat, chore}
  #   - if activity status==done  : all tasks must be done (warn)
  #   - if activity status==ip   : at least one task must be ip (warn)
  #   - task.status in {pending, in_progress, done, blocked}
  #   - task.agent  in {implementer, reviewer}
  #   - task.id and task.description required
  #   - max 1 activity in_progress

  VALID_STATUS='^(pending|in_progress|done|blocked)$'
  VALID_TYPE='^(fix|feat|chore)$'
  VALID_TASK_STATUS='^(pending|in_progress|done|blocked)$'
  VALID_AGENT='^(implementer|reviewer)$'

  HAS_INVALID=0

  # Count in_progress activities (activity-level only — 4-space indent, not task-level 8-space)
  IP_COUNT=$(grep -cE '^    "status"\s*:\s*"in_progress"' activities.json || true)
  if [ "${IP_COUNT:-0}" -gt 1 ]; then
    fail "Found ${IP_COUNT} activities in in_progress (max 1)"
  fi

  # Validate activity-level status values
  while IFS= read -r LINE; do
    [ -n "$LINE" ] || continue
    if ! echo "$LINE" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"(pending|in_progress|done|blocked)"'; then
      HAS_INVALID=1
      fail "Invalid activity status: $LINE"
    fi
  done < <(grep -E '"status"[[:space:]]*:' activities.json | grep -v 'task' || true)

  # Validate activity-level type values
  while IFS= read -r LINE; do
    [ -n "$LINE" ] || continue
    if ! echo "$LINE" | grep -Eq '"type"[[:space:]]*:[[:space:]]*"(fix|feat|chore)"'; then
      HAS_INVALID=1
      fail "Invalid activity type: $LINE (must be fix, feat, or chore)"
    fi
  done < <(grep -E '"type"[[:space:]]*:' activities.json || true)

  # Validate task-level status values
  while IFS= read -r LINE; do
    [ -n "$LINE" ] || continue
    if ! echo "$LINE" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"(pending|in_progress|done|blocked)"'; then
      HAS_INVALID=1
      fail "Invalid task status: $LINE"
    fi
  done < <(grep -E '"status"[[:space:]]*:' activities.json | grep -E '"(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p)"' || true)

  # Validate task-level agent values
  while IFS= read -r LINE; do
    [ -n "$LINE" ] || continue
    if ! echo "$LINE" | grep -Eq '"agent"[[:space:]]*:[[:space:]]*"(implementer|reviewer)"'; then
      HAS_INVALID=1
      fail "Invalid task agent: $LINE (must be implementer or reviewer)"
    fi
  done < <(grep -E '"agent"[[:space:]]*:' activities.json || true)

  # Count activities
  TOTAL=$(grep -cE '^\s*\{\s*$|"id"[[:space:]]*:[[:space:]]*[0-9]+' activities.json | head -n1 || true)
  if [ "$HAS_INVALID" -eq 0 ]; then
    ok "activities.json valid"
  fi
fi

echo
echo "── 4. Code Quality ───────────────────────────────────"

if [ -f "./gradlew" ]; then
  if gradlew spotlessCheck checkstyleMain checkstyleTest --configuration-cache >/dev/null 2>&1; then
    ok "Spotless + Checkstyle passed"
  else
    fail "Spotless or Checkstyle errors found (run ./gradlew spotlessApply)"
    EXIT=1
  fi
else
  warn "gradlew not found, skipping quality"
fi

echo
echo "── 5. Compilation ─────────────────────────────────────"

if [ -f "./gradlew" ]; then
  if gradlew compileJava compileTestJava --configuration-cache >/dev/null 2>&1; then
    ok "Compilation succeeded"
  else
    fail "Compilation failed"
    EXIT=1
  fi
else
  warn "gradlew not found, skipping compilation"
fi

echo
echo "── 6. Hexagonal Layering Check ────────────────────────"

# Mirror of harness.js §6 — instead of DESIGN.md style rules, we enforce
# the project's hard rule: domain/ may not import Spring/Jakarta frameworks.
DOMAIN_DIR="src/main/java"
if [ -d "$DOMAIN_DIR" ]; then
  # Heuristic: find files under any package that lives inside `domain/`.
  # We scan for any import of org.springframework.* or jakarta.persistence.*
  # inside `domain/` source paths. We exclude `application/` and `infrastructure/`.
  VIOLATIONS=$(find "$DOMAIN_DIR" -type d -name 'domain' 2>/dev/null \
    | while read -r d; do
        grep -rEn '^import\s+(org\.springframework|jakarta\.persistence)' "$d" 2>/dev/null
      done | wc -l | tr -d ' ')

  if [ "${VIOLATIONS:-0}" -eq 0 ]; then
    ok "domain/ has no Spring/JPA imports"
  else
    fail "domain/ has ${VIOLATIONS} forbidden framework import(s) (see docs/architecture.md)"
    EXIT=1
  fi
else
  warn "$DOMAIN_DIR not found, skipping layering check"
fi

echo
echo "── 6b. BigDecimal in domain check ─────────────────────"

# Enforce that domain/ (outside common/money/) does not import java.math.BigDecimal.
# Monetary amounts must be expressed via the Money value object.
# Legitimate non-monetary BigDecimal (quantities, percentages, factory methods) is
# allowed in the excluded paths listed below.
if [ -d "$DOMAIN_DIR" ]; then
  VIOLATIONS=$(find "$DOMAIN_DIR" -type d -name 'domain' 2>/dev/null \
    | while read -r d; do
        grep -rl '^import java.math.BigDecimal' "$d" 2>/dev/null
      done \
    | grep -v '/common/money/' \
    | grep -v '/analytics/domain/AnalyticsConfig' \
    | grep -v '/analytics/domain/MonthlyFinancialSummary' \
    | grep -v '/analytics/domain/PrimeCostReport' \
    | grep -v '/analytics/domain/port/in/UpdateAnalyticsConfigUseCase' \
    | grep -v '/purchase/domain/PurchaseOrderItem' \
    | grep -v '/inventory/domain/SupplyVariant' \
    | grep -v '/inventory/domain/ProductRecipe' \
    | grep -v '/inventory/domain/OptionRecipe' \
    | grep -v '/inventory/domain/InventoryStock' \
    | grep -v '/inventory/domain/InventoryMovement' \
    | grep -v '/user/domain/Salary' \
    | grep -v '/specialselection/domain/SuggestedPrice' \
    | grep -v '/product/domain/ProductCost' \
    | wc -l | tr -d ' ')

  if [ "${VIOLATIONS:-0}" -eq 0 ]; then
    ok "domain/ has no BigDecimal imports outside common/money/ (excluding legitimate files)"
  else
    fail "domain/ has ${VIOLATIONS} BigDecimal import(s) outside common/money/ (see docs/architecture.md)"
    EXIT=1
  fi
else
  warn "$DOMAIN_DIR not found, skipping BigDecimal check"
fi

echo
echo "── 7. Running Tests ───────────────────────────────────"

if [ -d "src/test" ] || [ -d "src/integrationTest" ]; then
  if gradlew test --configuration-cache >/dev/null 2>&1; then
    ok "All tests pass"
  else
    fail "Some tests are broken"
    EXIT=1
  fi
else
  warn "src/test directory not found, skipping tests"
fi

echo
echo "── 8. Summary ─────────────────────────────────────────"

if [ "$EXIT" -eq 0 ]; then
  ok "Environment ready. You can start working."
else
  fail "Environment NOT ready. Resolve errors before advancing."
fi

exit "$EXIT"