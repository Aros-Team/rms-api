# Current Session

## Activity
- ID: 3
- Name: fix-frontend-combo-gaps
- Type: feat
- Status: in_progress

## Tasks
- Current: a, c, d, e, f (parallel)
- Pending: b, g

## Plan

Address 8 gaps from docs/COMBO_BACKEND_VERIFICATION.md:
- #6 order response (a)
- #10 websocket envelope (c)
- #9 active-toggle PATCH (d part 1)
- #3 suggested-price enrichment (d part 2)
- #4 products filter + selectionType/imageUrl (e)
- #7 question types (f)
- #11 schedule enforcement on orders (b)
- #2 suggest-price endpoint (kept POST; documented)

## Notes
- Each implementer writes results to `progress/explore/<task>.md` per Anti-Teléfono-Descompuesto rule
- They return only file references
- Final harness verification in task g

## Blockers
- Baseline `./harness/harness.sh` fails section 3: `harness/harness.sh:133` counts every `in_progress` status, including task a plus activity 3, and reports 2 activities. Sections 4-7 remain `[OK]`. Task a implementation paused before source edits pending leader guidance.
---