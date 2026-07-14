#!/usr/bin/env bash
# build-harness.sh — bootstraps the harness structure.
# Mirrors scripts/build_harness.js from rms/frontend.
#
# Usage:  ./harness/build-harness.sh [project_name] [description]
# Defaults pulled from build.gradle (project.name) when available.

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

PROJECT_NAME="${1:-rms-api}"
DESCRIPTION="${2:-RMS API activities and progress tracking}"

mkdir -p progress
mkdir -p docs
mkdir -p harness/activities

# ---------- activities.json ----------
# Top-level array — schema enforced by harness.sh / harness.bat.
# Reference: .opencode/agent/main-orchestrator.md (Activity Entry Structure)
if [ ! -f "activities.json" ]; then
  printf '[]\n' > activities.json
  echo "Created activities.json"
else
  echo "activities.json already exists"
fi

# ---------- progress/current.md ----------
if [ ! -f "progress/current.md" ]; then
  cat > progress/current.md <<'EOF'
# Current Session

## Activity
- ID:
- Name:
- Type:
- Status:

## Tasks
- Current:
- Pending:

## Plan

## Notes

## Blockers

---
EOF
  echo "Created progress/current.md"
else
  echo "progress/current.md already exists"
fi

# ---------- activities.json example comment ----------
# Once an activity is created it follows the schema documented in
# .opencode/agent/main-orchestrator.md (id:int, type, name kebab-case,
# title, description, acceptance[], tasks[{id,description,status,agent}],
# status). See that file for a full example.

# ---------- progress/history.md ----------
if [ ! -f "progress/history.md" ]; then
  cat > progress/history.md <<'EOF'
# Session History

---
EOF
  echo "Created progress/history.md"
else
  echo "progress/history.md already exists"
fi

echo
echo "Harness bootstrap complete."
echo "Next: ./harness/harness.sh"