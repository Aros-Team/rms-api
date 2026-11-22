# Seed follow-ups lane — cohort realism, alerts, payment-aware reset (P2-SEED-13/14, P3-BE-PAY-08/09)

Branch `feat/seed-followups`, worktree `/home/jorge/Projects/aros/worktrees/api-seed-followups`
(cut from `dev`). No commit. No writes to the dev database (`rmsdb`, 3306) and no changes to the
running API; every reset and verification ran against the scratch schema `rms_seed_followups` on
the test MySQL 8 container (`rms-dev-db-test`, 3307).

## 1. What changed

| ID | Change | File |
|---|---|---|
| P2-SEED-13 | Every order gets a deterministic customer identity: 66% normalized phones from a 45,000-identity pool, fingerprint = SHA-256(phone); 34% stay table-only | `seed/SeedMath`, `seed/SeedRunner` (`OrderWriter`), `seed/SeedPlan` |
| P2-SEED-14 | `seedAlerts()` rebuilds `system_alerts` after every reset: 4 OPEN rows mirroring the G5 rule set + 1 READ row for E15 | `seed/SeedRunner` |
| P3-BE-PAY-08 | `seedCashAndPayments()` writes 30 closed daily shifts with payments on the 30 days before the anchor, plus one OPEN shift with a 200,000 COP float; today's delivered orders stay unpaid | `seed/SeedRunner` |
| P3-BE-PAY-09 | Reset deletes `payment_tenders → payments → cash_movements → cash_shifts` before `orders` | `seed/SeedRunner` (`deleteSeedOwnedRows`) |
| — | `seedVerify` grew from 29 to 49 checks (cohort strategy, alerts, payments, shifts) | `seed/SeedVerifier` |
| — | Status output includes alerts, shifts and payments | `seed/SeedCli`, `seed/SeedResetService` |
| — | Customer identity unit test | `seed/SeedPlanTest` |

One pre-existing checkstyle defect on the branch tip was fixed so the gate can pass: the Javadoc of
`OrderController.getOrders` had been attached to `findById` (comment-only change, no behaviour).

### Customer identity

```
phone?        crc("20261121:phone:" + orderId) mod 100 < 66        → 66% of orders
identity      crc("20261121:customer:" + orderId) mod 45,000
phone         3 + zero-padded index (10-digit Colombian mobile)
fingerprint   SHA-256(phone), lowercase hex, identical to MySQL SHA2(phone, 256)
```

No order aggregates change: the phone columns do not participate in revenue, COGS, stock or
payroll. Repeat visits come from the fixed identity pool, so the recurring rate is deterministic
and realistic instead of being an artifact of 20 table ids.

### Alert keys

The monthly/weekly keys move with the anchor, so a rolling reset never leaves stale periods:

| Severity | Type | Metric | Period | Status |
|---|---|---|---|---|
| RED | `FOOD_COST_DEVIATION` | `food_cost_pct` | previous month | OPEN |
| RED | `REVPASH_DROP` | `revpash` | week − 1 | OPEN |
| YELLOW | `SALES_DROP_YOY` | `sales_orders` | anchor month | OPEN |
| YELLOW | `LOW_MARGIN_SKU_SPIKE` | `dog_units` | week − 1 | OPEN |
| YELLOW | `LOW_MARGIN_SKU_SPIKE` | `dog_units` | week − 2 | READ |

The four OPEN rows reproduce the G5 type/severity mix (`page.total = 4`, RED 2, YELLOW 2), and the
READ row covers the mark-read/dismissed edge case (E15).

### Payment demo data

- 30 closed shifts (`anchor − 30 … anchor − 1`), one cashier per day (1013/1014), float 200,000.
- One payment per delivered historical order in that window: subtotal = Σ `order_details.unit_price`,
  0 discount, deterministic tip on 30% of orders, total = subtotal + tip.
- Tenders: 45% cash (rounded up to 5,000 with change), 35% card, 10% transfer, 10% cash+card split.
  Tenders always sum to the total; only the last cash tender may overpay.
- Receipts `R-yyyyMMdd-<last 6 digits of order id>`, idempotency key `seed-pay-<orderId>`.
- `expected_cash = float + cash tenders − change`; `counted_cash = expected + {−1,0,+1}×1,000`;
  both and the difference are snapshotted per closed shift.
- The only OPEN shift is the anchor day's shift (opened 08:00, or midnight when the reset runs
  before 08:00). No payment is created for any order dated on the anchor day.

## 2. Commands

```bash
cd /home/jorge/Projects/aros/worktrees/api-seed-followups

# create the scratch schema once
docker exec rms-dev-db-test mysql -uroot -p123 \
  -e "DROP DATABASE IF EXISTS rms_seed_followups; CREATE DATABASE rms_seed_followups CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

URL='jdbc:mysql://localhost:3307/rms_seed_followups?useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true&connectionTimeZone=America/Bogota&forceConnectionTimeZoneToSession=true'

./gradlew seedReset  -Panchor=2026-11-21 -PdbUrl="$URL" -PdbPassword=123
./gradlew seedVerify -Panchor=2026-11-21 -PdbUrl="$URL" -PdbPassword=123

./gradlew spotlessApply checkstyleMain checkstyleTest test
./harness/harness.sh
```

The reset was executed **twice** with the same anchor after payments existed, which exercises
P3-BE-PAY-09 (FK order / delete list) end to end.

## 3. Produced counts (anchor 2026-11-21, last run)

| Metric | Value |
|---|---:|
| historical orders / lines / revenue / covers | 17,135 / 59,977 / 1,435,464,200 / 46,229 |
| orders total (historical + operational day) | 17,271 (operational day varies with the clock) |
| historical orders with phone | **11,325 (66.10%)** |
| historical table-only orders | **5,810 (33.90%)** |
| `system_alerts` | **5** (4 OPEN + 1 READ, 4 distinct OPEN types) |
| `cash_shifts` | **31** (30 CLOSED + 1 OPEN, float 200,000) |
| `payments` / `payment_tenders` | **3,152 / 3,459** |
| charged / tips / change | 272,848,300 / 8,011,000 / 2,951,900 COP |
| closed-shift expected / counted / difference | 140,297,100 / 140,302,100 / +5,000 COP |
| tender mix | CASH 1,728 (137,249,000), CARD 1,408 (111,166,200), TRANSFER 323 (27,385,000) |

Cohort check on 2026-10 (90-day lookback, app timezone):

| Metric | Value |
|---|---:|
| orders / phones | 3,306 / 2,164 (65.46%) |
| distinct fingerprints | 2,103 |
| recurring / new | 1,394 / 1,912 |
| recurring rate | **42.17%** (spec target 20–45%) |
| `fingerprintStrategy` | **`PHONE_OR_TABLE`** (not `TABLE_ONLY`) |

## 4. Verification

`./gradlew seedVerify -Panchor=2026-11-21` → **49/49 checks pass**. New checks (20):

| Check | Expected | Actual |
|---|---:|---:|
| historical phone orders | 11,325 | 11,325 |
| historical table-only orders | 5,810 | 5,810 |
| phone fingerprints consistent | 0 mismatches | 0 |
| cohort strategy phone or table | 3 (`PHONE_OR_TABLE`) | 3 |
| alerts present / open / read | 5 / 4 / 1 | 5 / 4 / 1 |
| distinct open alert types | 4 | 4 |
| payments | 3,152 | 3,152 |
| closed / open cash shifts | 30 / 1 | 30 / 1 |
| open shift float | 200,000 | 200,000 |
| today orders unpaid | 0 | 0 |
| no double active payments | 0 | 0 |
| payment subtotal matches order | 0 | 0 |
| payment total arithmetic | 0 | 0 |
| tenders sum to tendered | 0 | 0 |
| payment change arithmetic | 0 | 0 |
| shift expected cash | 0 | 0 |
| shift difference arithmetic | 0 | 0 |

Determinism: a second identical reset produced the same historical counts, revenue and phone
identities, and byte-identical content checksums for every payment-owned row (`payments`
`6798569810076`, `payment_tenders` `7636325946196`, `cash_shifts` `83576348274`,
`system_alerts` `6614016463`). Only the operational-day order count moves (17,269 → 17,271), which
is the documented clock-relative behaviour of section 4.4 of the seed spec.

Quality gate:

```
./gradlew spotlessApply checkstyleMain checkstyleTest test
BUILD SUCCESSFUL — 150 suites, 865 tests, 0 failures, 0 errors, 1 skipped
```

```
./harness/harness.sh
...
[OK] Spotless + Checkstyle passed
[OK] Compilation succeeded
[OK] domain/ has no Spring/JPA imports
[OK] All test files have Javadoc headers
[OK] All tests pass
[OK] Environment ready. You can start working.
HARNESS_EXIT=0
```

`activities.json` and `progress/current.md` are git-ignored coordination files that exist only in
the main checkout; they were copied into the worktree (untracked) so the harness could validate
them. They are not part of the branch.

## 5. What remains / notes

1. **Alerts are seeded demo rows, not rule-engine output.** The CLI seeder has no Spring context,
   so it writes the documented G5-shaped demo rows. `GenerateAlertsService` (04:00 job and
   `POST /api/v1/analytics/refresh`) remains the single place that evaluates real rules and
   deduplicates them; seeded rows are replaced on the next reset.
2. **`SALES_DROP_YOY` stays cold-start** until a prior-year dataset exists; the demo row documents
   the rule without claiming a real year-over-year comparison.
3. **Payment history covers only `anchor − 30 … anchor − 1`.** Today's delivered orders are
   deliberately unpaid so the cashier pending board is non-empty; anything older is also unpaid and
   can be charged during a demo.
4. **`cash_shifts.opened_at`** for the OPEN shift is anchor 08:00 (midnight when the reset runs
   before 08:00), the only clock-aware value, consistent with the existing operational-day rule.
   Closed shifts snapshot `expected`, `counted` and `difference`, so a later reset cannot rewrite a
   Z-report retroactively.
5. **`orders.date` is a `TIMESTAMP`.** Ad-hoc SQL must run with `SET time_zone='America/Bogota'`
   (the app connection already does); a UTC session shifts business dates by five hours. The
   verifier pins the timezone, as before.
6. **No changes** to payment/analytics business logic, controllers, DTOs, contracts or migrations;
   only the seed, its tests and the one Javadoc fix above.
