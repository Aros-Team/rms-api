# Seed catalog (`catalog.sql`)

Master data only: areas are created by Flyway `V23`; this script adds the floor,
categories, supplies, recipes and the 27-product catalogue (including 3 combos).

## Why it is separate from `data.sql`

`data.sql` used to contain master data **and** a destructive April–July demo seed, and
`spring.sql.init.mode=always` re-ran it on every boot. Boot no longer seeds; `data.sql`
is now intentionally empty and the mode is `never` (development profile).

## Procedure

```bash
# 1. Migrate + load the catalog + generate 180 days (rolling anchor = today)
./gradlew seedReset

# 2. Fixed reference anchor (golden dataset)
./gradlew seedReset -Panchor=2026-11-21

# 3. Verify invariants and reference counts
./gradlew seedVerify -Panchor=2026-11-21

# Against the test container on 3307 (never against rmsdb on 3306 while the app runs)
./gradlew seedReset \
  -PdbUrl='jdbc:mysql://localhost:3307/rms_seed_test?useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true&connectionTimeZone=America/Bogota&forceConnectionTimeZoneToSession=true' \
  -PdbPassword=123
```

The catalog is idempotent (`INSERT IGNORE` / `ON DUPLICATE KEY`). It is executed by
`SeedRunner` inside `seedReset`, never during application boot.

The `ALTER TABLE ... AUTO_INCREMENT = 1` prologue only affects **empty** tables. It
keeps catalogue ids deterministic if a previous reset was rolled back and MySQL kept
the advanced counter.
