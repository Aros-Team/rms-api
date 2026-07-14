@echo off
REM ============================================================
REM  build-harness.bat - bootstraps the harness structure.
REM  Mirrors scripts/build_harness.js from rms/frontend.
REM
REM  Usage:   harness\build-harness.bat [project_name] [description]
REM ============================================================

setlocal EnableExtensions

set "PROJECT_ROOT=%~dp0.."
cd /d "%PROJECT_ROOT%"

set "PROJECT_NAME=rms-api"
set "DESCRIPTION=RMS API activities and progress tracking"
if not "%~1"=="" set "PROJECT_NAME=%~1"
if not "%~2"=="" set "DESCRIPTION=%~2"

if not exist "progress" mkdir "progress"
if not exist "docs" mkdir "docs"
if not exist "harness\activities" mkdir "harness\activities"

REM ---------- activities.yml ----------
REM Top-level YAML registry (one entry per feature/fix/chore the user wants done).
REM Read by main-orchestrator (.opencode/agent/), validated by harness.bat.
if not exist "activities.yml" (
    > activities.yml echo ---
    echo Created activities.yml
) else (
    echo activities.yml already exists
)

REM ---------- progress/current.md ----------
if not exist "progress\current.md" (
    > progress\current.md (
        echo # Current Session
        echo.
        echo ## Activity
        echo - ID:
        echo - Name:
        echo - Type:
        echo - Status:
        echo.
        echo ## Tasks
        echo - Current:
        echo - Pending:
        echo.
        echo ## Plan
        echo.
        echo ## Notes
        echo.
        echo ## Blockers
        echo.
        echo ---
    )
    echo Created progress/current.md
) else (
    echo progress/current.md already exists
)

REM ---------- progress/history.md ----------
if not exist "progress\history.md" (
    > progress\history.md (
        echo # Session History
        echo.
        echo ---
    )
    echo Created progress/history.md
) else (
    echo progress/history.md already exists
)

echo.
echo Harness bootstrap complete.
echo Next: harness\harness.bat

endlocal