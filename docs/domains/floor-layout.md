# Floor Layout

> Schematic physical distribution of the restaurant areas.

## Concept

The floor layout is one restaurant-wide grid. Each region is an independent
physical representation of an existing Area. The editor is intentionally
schematic: no meters, walls, doors or furniture.

A region belongs to exactly one Area. An Area may be represented by several
independent regions. Cells cannot be shared by two regions.

## API

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/floor-layout` | Current layout (or 32×20 defaults) |
| PUT | `/api/v1/floor-layout` | Validate and atomically replace the layout |

Both endpoints require the admin role.

```json
{
  "columns": 32,
  "rows": 20,
  "regions": [
    {
      "key": "6f0c2b4e-...",
      "areaId": 3,
      "segments": [
        { "row": 1, "from": 2, "to": 6 },
        { "row": 2, "from": 2, "to": 4 }
      ]
    }
  ]
}
```

The response also carries `updatedAt`.

## Geometry contract

- Origin is the **top-left**.
- `row` is zero-based and grows downward.
- `from` and `to` are inclusive zero-based column bounds.
- `0 <= row < rows`, `0 <= from <= to < columns`.
- Segments are sorted by row and column.
- Overlapping or adjacent runs on the same row are merged into maximal runs.
- Duplicate cells inside one region are unioned; the same cell in two regions
  is rejected.
- An empty segment list is invalid.
- The editor grows the grid automatically as regions approach the edge; there
  is no user-facing fixed size. Dimensions start at 32×20 and the API keeps a
  safety cap of 100,000×100,000.
- `key` is a stable client-generated UUID (max 64 chars) and is unique per
  layout.

Storage uses one `floor_layout_region` row per region with
`segments_json LONGTEXT`. It never stores one row per cell.

## Persistence and integrity

- `floor_layout` is a singleton row (`id = 1`).
- Deleting the whole layout row removes its regions.
- `floor_layout_region.area_id` uses `ON DELETE RESTRICT`: an Area cannot be
  deleted while a layout region references it. Layout data is never silently
  removed when an Area disappears.
- The API currently has no `DELETE /areas` endpoint. If one is introduced, it
  must pre-check floor layout references and return **409 Conflict** until the
  regions are explicitly removed or reassigned.
- `PUT /floor-layout` is a full replacement inside one transaction. The entire
  document is validated before the adapter deletes and reinserts regions; a
  failure rolls back to the previous state.

## Code

- Backend: `core/floorlayout`, `infraestructure/floorlayout`.
- Migration: `V73__create_floor_layout.sql`.
- Frontend: `areas/admin/features/manage/features/areas/layout`.
- Frontend color: deterministic `--rms-chart-1..8` mapping from `areaId`; no
  color is stored.


## Table placements

Tables are placed on the same grid but persisted independently from area
regions, so editing the area map never deletes the tables.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/floor-layout/tables` | Current table placements |
| PUT | `/api/v1/floor-layout/tables` | Validate and atomically replace them |

```json
[
  { "tableId": 1, "row": 9, "column": 19 }
]
```

Validation:

- Every table id must exist.
- A table occupies one unique cell; two tables cannot share a cell.
- The cell must belong to a painted region whose area is enabled and has the
  `SERVE` activity (`Atención al cliente`).
- The table id is unique in the placement list.

Placements use `ON DELETE CASCADE` from `tables`, so deleting a table removes
its placement.
