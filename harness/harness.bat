@echo off
REM ============================================================
REM  harness.bat - environment + code quality verifier
REM  Mirrors scripts/harness.js from rms/frontend, adapted for
REM  Spring Boot / Gradle / Docker on Windows.
REM
REM  Usage:   harness\harness.bat
REM ============================================================

setlocal EnableExtensions EnableDelayedExpansion

set "PROJECT_ROOT=%~dp0.."
cd /d "%PROJECT_ROOT%"

set "EXIT=0"

REM -- colors (Windows 10+ ANSI) -----------------------------
for /F "tokens=*" %%i in ('echo prompt $E ^| cmd') do set "ESC=%%i"
set "RED=!ESC![0;31m"
set "GREEN=!ESC![0;32m"
set "YELLOW=!ESC![0;33m"
set "NC=!ESC![0m"

echo ── 1. Environment Check ─────────────────────────────

where java >nul 2>&1
if errorlevel 1 (
    echo !RED![FAIL]!NC!  java is not installed
    exit /b 1
)
for /F "tokens=*" %%v in ('java -version 2^>^&1') do (
    echo !GREEN![OK]!NC!    java -^> %%v
    goto :java_done
)
:java_done

if not exist "gradlew.bat" (
    echo !RED![FAIL]!NC!  gradlew.bat not found at repo root
    exit /b 1
)
echo !GREEN![OK]!NC!    gradlew.bat -^> present

where docker >nul 2>&1
if errorlevel 1 (
    echo !YELLOW![WARN]!NC!  docker is not installed (compose-dependent tasks will fail^)
) else (
    for /F "tokens=*" %%v in ('docker --version') do echo !GREEN![OK]!NC!    docker -^> %%v
    docker compose version >nul 2>&1
    if errorlevel 1 (
        echo !YELLOW![WARN]!NC!  docker compose plugin not detected
    ) else (
        for /F "tokens=*" %%v in ('docker compose version') do echo !GREEN![OK]!NC!    docker compose -^> %%v
    )
)

echo.
echo ── 2. Base Harness Files ─────────────────────────────

set "MISSING=0"
for %%f in (
    AGENTS.md
    activities.json
    .opencode\agent\main-orchestrator.md
    .opencode\agent\task-executor.md
    .opencode\agent\implementation-reviewer.md
    progress\current.md
    progress\history.md
    docs\architecture.md
    docs\conventions.md
    docs\verification.md
    docs\CHECKPOINTS.md
) do (
    if not exist "%%f" (
        echo !RED![FAIL]!NC!  Missing base file: %%f
        set "MISSING=1"
        set "EXIT=1"
    ) else (
        echo !GREEN![OK]!NC!    Exists %%f
    )
)

echo.
echo ── 3. Validating activities.json ─────────────────────

if not exist "activities.json" (
    echo !RED![FAIL]!NC!  activities.json not found
    set "EXIT=1"
) else (
    REM -- Empty array is valid
    findstr /B /L /C:"[" activities.json >nul 2>&1
    if errorlevel 1 (
        echo !RED![FAIL]!NC!  activities.json does not start with [
        set "EXIT=1"
    )

    REM -- Brace balance
    set "OPEN=0"
    set "CLOSE=0"
    for /F %%c in ('find /C "{" ^< activities.json') do set "OPEN=%%c"
    for /F %%c in ('find /C "}" ^< activities.json') do set "CLOSE=%%c"
    if !OPEN! neq !CLOSE! (
        echo !RED![FAIL]!NC!  activities.json braces unbalanced ^({=!OPEN!, }=!CLOSE!^)
        set "EXIT=1"
    )

    REM -- Validate status enums at activity level
    for /F "tokens=*" %%L in ('findstr /C:"\"status\":" activities.json') do (
        echo %%L | findstr /R /C:"\"status\": *\"pending\""   >nul
        if errorlevel 1 (
            echo %%L | findstr /R /C:"\"status\": *\"in_progress\"" >nul
            if errorlevel 1 (
                echo %%L | findstr /R /C:"\"status\": *\"done\""      >nul
                if errorlevel 1 (
                    echo %%L | findstr /R /C:"\"status\": *\"blocked\""  >nul
                    if errorlevel 1 (
                        echo !YELLOW![WARN]!NC!  Review status line: %%L
                    )
                )
            )
        )
    )

    REM -- Validate type enums
    for /F "tokens=*" %%L in ('findstr /C:"\"type\":" activities.json') do (
        echo %%L | findstr /R /E /C:"\"type\": *\"fix\""   >nul
        if errorlevel 1 (
            echo %%L | findstr /R /E /C:"\"type\": *\"feat\""  >nul
            if errorlevel 1 (
                echo %%L | findstr /R /E /C:"\"type\": *\"chore\"" >nul
                if errorlevel 1 (
                    echo !RED![FAIL]!NC!  Invalid type line: %%L
                    set "EXIT=1"
                )
            )
        )
    )

    REM -- Validate agent enums
    for /F "tokens=*" %%L in ('findstr /C:"\"agent\":" activities.json') do (
        echo %%L | findstr /R /E /C:"\"agent\": *\"implementer\"" >nul
        if errorlevel 1 (
            echo %%L | findstr /R /E /C:"\"agent\": *\"reviewer\""    >nul
            if errorlevel 1 (
                echo !RED![FAIL]!NC!  Invalid agent line: %%L
                set "EXIT=1"
            )
        )
    )

    REM -- Count in_progress activities (rough)
    set "IP_COUNT=0"
    for /F "tokens=*" %%L in ('findstr /C:"\"status\": *\"in_progress\"" activities.json') do (
        set /a "IP_COUNT+=1" >nul
    )
    if !IP_COUNT! gtr 1 (
        echo !RED![FAIL]!NC!  Found !IP_COUNT! activities in in_progress ^(max 1^)
        set "EXIT=1"
    )

    if !EXIT! equ 0 (
        echo !GREEN![OK]!NC!    activities.json schema looks valid
    )
)

echo.
echo ── 4. Code Quality ───────────────────────────────────

call gradlew.bat spotlessCheck checkstyleMain checkstyleTest --configuration-cache >nul 2>&1
if errorlevel 1 (
    echo !RED![FAIL]!NC!  Spotless or Checkstyle errors found ^(run gradlew spotlessApply^)
    set "EXIT=1"
) else (
    echo !GREEN![OK]!NC!    Spotless + Checkstyle passed
)

echo.
echo ── 5. Compilation ─────────────────────────────────────

call gradlew.bat compileJava compileTestJava --configuration-cache >nul 2>&1
if errorlevel 1 (
    echo !RED![FAIL]!NC!  Compilation failed
    set "EXIT=1"
) else (
    echo !GREEN![OK]!NC!    Compilation succeeded
)

echo.
echo ── 6. Hexagonal Layering Check ────────────────────────

if exist "src\main\java" (
    set "VIOLATIONS=0"
    for /F "tokens=*" %%d in ('dir /S /B /AD "src\main\java" ^| findstr /I /R "domain$"') do (
        for /F "tokens=*" %%f in ('findstr /S /R /C:"^import org.springframework" /C:"^import jakarta.persistence" "%%d\*.java" 2^>nul') do (
            set /a "VIOLATIONS+=1" >nul
        )
    )
    if !VIOLATIONS! equ 0 (
        echo !GREEN![OK]!NC!    domain/ has no Spring/JPA imports
    ) else (
        echo !RED![FAIL]!NC!  domain/ has !VIOLATIONS! forbidden framework import^(s^)
        set "EXIT=1"
    )
) else (
    echo !YELLOW![WARN]!NC!  src\main\java not found, skipping layering check
)

echo.
echo ── 7. Running Tests ───────────────────────────────────

if exist "src\test" (
    call gradlew.bat test --configuration-cache >nul 2>&1
    if errorlevel 1 (
        echo !RED![FAIL]!NC!  Some tests are broken
        set "EXIT=1"
    ) else (
        echo !GREEN![OK]!NC!    All tests pass
    )
) else (
    echo !YELLOW![WARN]!NC!  src\test directory not found, skipping tests
)

echo.
echo ── 8. Summary ─────────────────────────────────────────

if !EXIT! equ 0 (
    echo !GREEN![OK]!NC!    Environment ready. You can start working.
) else (
    echo !RED![FAIL]!NC!  Environment NOT ready. Resolve errors before advancing.
)

exit /b %EXIT%