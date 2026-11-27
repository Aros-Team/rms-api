/* (C) 2026 */

package aros.services.rms.core.floorlayout.application.service;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.domain.AreaActivity;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.floorlayout.domain.FloorGeometry;
import aros.services.rms.core.floorlayout.domain.FloorLayout;
import aros.services.rms.core.floorlayout.domain.FloorLayoutLimits;
import aros.services.rms.core.floorlayout.domain.FloorRegion;
import aros.services.rms.core.floorlayout.domain.FloorSegment;
import aros.services.rms.core.floorlayout.domain.FloorTablePlacement;
import aros.services.rms.core.floorlayout.domain.exception.InvalidFloorLayoutException;
import aros.services.rms.core.floorlayout.domain.exception.InvalidFloorTablePlacementException;
import aros.services.rms.core.floorlayout.port.input.FloorLayoutUseCase;
import aros.services.rms.core.floorlayout.port.output.FloorLayoutRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates and persists the restaurant floor layout.
 *
 * <p>The complete document is validated before the adapter is called, so an invalid submission can
 * never leave a partially replaced layout.
 */
public class FloorLayoutService implements FloorLayoutUseCase {

  private final FloorLayoutRepositoryPort floorLayoutRepositoryPort;
  private final AreaRepositoryPort areaRepositoryPort;
  private final TableRepositoryPort tableRepositoryPort;

  /**
   * Creates the floor layout service.
   *
   * @param floorLayoutRepositoryPort layout persistence port
   * @param areaRepositoryPort existing area lookup
   * @param tableRepositoryPort existing table lookup
   */
  public FloorLayoutService(
      FloorLayoutRepositoryPort floorLayoutRepositoryPort,
      AreaRepositoryPort areaRepositoryPort,
      TableRepositoryPort tableRepositoryPort) {
    this.floorLayoutRepositoryPort = floorLayoutRepositoryPort;
    this.areaRepositoryPort = areaRepositoryPort;
    this.tableRepositoryPort = tableRepositoryPort;
  }

  /** {@inheritDoc} */
  @Override
  public FloorLayout get() {
    return floorLayoutRepositoryPort.find().orElseGet(FloorLayout::defaults);
  }

  /** {@inheritDoc} */
  @Override
  public FloorLayout save(FloorLayout layout) {
    if (layout == null) {
      throw new InvalidFloorLayoutException("La distribución es obligatoria");
    }
    validateDimensions(layout.getColumns(), layout.getRows());

    List<FloorRegion> normalized =
        (layout.getRegions() == null ? List.<FloorRegion>of() : layout.getRegions())
            .stream().map(this::normalizeRegion).toList();

    FloorGeometry.validateRegions(normalized, layout.getColumns(), layout.getRows());
    validateAreas(normalized);

    FloorLayout canonical =
        FloorLayout.builder()
            .columns(layout.getColumns())
            .rows(layout.getRows())
            .regions(normalized)
            .build();
    return floorLayoutRepositoryPort.save(canonical);
  }

  private FloorRegion normalizeRegion(FloorRegion region) {
    if (region == null) {
      throw new InvalidFloorLayoutException("La distribución contiene una región vacía");
    }
    return FloorRegion.builder()
        .key(region.getKey())
        .areaId(region.getAreaId())
        .segments(FloorGeometry.normalize(region.getSegments()))
        .build();
  }

  private void validateDimensions(int columns, int rows) {
    if (columns < FloorLayoutLimits.MIN_COLUMNS || columns > FloorLayoutLimits.MAX_COLUMNS) {
      throw new InvalidFloorLayoutException(
          "El lienzo debe tener entre "
              + FloorLayoutLimits.MIN_COLUMNS
              + " y "
              + FloorLayoutLimits.MAX_COLUMNS
              + " columnas");
    }
    if (rows < FloorLayoutLimits.MIN_ROWS || rows > FloorLayoutLimits.MAX_ROWS) {
      throw new InvalidFloorLayoutException(
          "El lienzo debe tener entre "
              + FloorLayoutLimits.MIN_ROWS
              + " y "
              + FloorLayoutLimits.MAX_ROWS
              + " filas");
    }
  }

  private void validateAreas(List<FloorRegion> regions) {
    Set<Long> areaIds =
        regions.stream().map(FloorRegion::getAreaId).collect(Collectors.toCollection(HashSet::new));
    if (areaIds.isEmpty()) {
      return;
    }

    Set<Long> existing =
        areaRepositoryPort.findByIdIn(List.copyOf(areaIds)).stream()
            .map(Area::getId)
            .collect(Collectors.toSet());
    Set<Long> missing = new HashSet<>(areaIds);
    missing.removeAll(existing);
    if (!missing.isEmpty()) {
      throw new InvalidFloorLayoutException(
          "Alguna de las áreas seleccionadas ya no existe. "
              + "Recarga la página e inténtalo de nuevo.");
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<FloorTablePlacement> getTablePlacements() {
    return floorLayoutRepositoryPort.findTablePlacements();
  }

  /** {@inheritDoc} */
  @Override
  public List<FloorTablePlacement> saveTablePlacements(List<FloorTablePlacement> placements) {
    if (placements == null) {
      throw new InvalidFloorTablePlacementException("La lista de mesas es obligatoria");
    }

    FloorLayout layout = get();
    Map<Long, Long> areaByCell = buildAreaByCell(layout);
    Map<Long, Table> tables = loadTables(placements);
    Map<Long, Area> areas = loadAreas(areaByCell);

    Set<Long> tableIds = new HashSet<>();
    Set<Long> occupiedCells = new HashSet<>();
    for (FloorTablePlacement placement : placements) {
      if (placement == null || placement.getTableId() == null) {
        throw new InvalidFloorTablePlacementException("Cada mesa debe tener un identificador");
      }
      if (!tableIds.add(placement.getTableId())) {
        throw new InvalidFloorTablePlacementException("Hay mesas duplicadas en el plano");
      }
      Table table = tables.get(placement.getTableId());
      if (table == null) {
        throw new InvalidFloorTablePlacementException(
            "La mesa " + placement.getTableId() + " ya no existe");
      }
      if (placement.getRow() < 0
          || placement.getRow() >= layout.getRows()
          || placement.getColumn() < 0
          || placement.getColumn() >= layout.getColumns()) {
        throw new InvalidFloorTablePlacementException(
            "La mesa " + table.getTableNumber() + " queda fuera del lienzo");
      }
      long cell = (long) placement.getRow() * layout.getColumns() + placement.getColumn();
      if (!occupiedCells.add(cell)) {
        throw new InvalidFloorTablePlacementException("Dos mesas no pueden ocupar la misma celda");
      }
      Long areaId = areaByCell.get(cell);
      Area area = areaId == null ? null : areas.get(areaId);
      if (area == null) {
        throw new InvalidFloorTablePlacementException(
            "La mesa " + table.getTableNumber() + " debe estar dentro de un área dibujada");
      }
      if (!area.isEnabled()
          || area.getActivities() == null
          || !area.getActivities().contains(AreaActivity.SERVE)) {
        throw new InvalidFloorTablePlacementException(
            "La mesa "
                + table.getTableNumber()
                + " solo puede ubicarse en un área habilitada de atención al cliente");
      }
    }

    List<FloorTablePlacement> canonical =
        placements.stream()
            .sorted((left, right) -> left.getTableId().compareTo(right.getTableId()))
            .toList();
    return floorLayoutRepositoryPort.saveTablePlacements(canonical);
  }

  private Map<Long, Long> buildAreaByCell(FloorLayout layout) {
    Map<Long, Long> areaByCell = new HashMap<>();
    if (layout.getRegions() == null) {
      return areaByCell;
    }
    for (FloorRegion region : layout.getRegions()) {
      if (region.getSegments() == null) {
        continue;
      }
      for (FloorSegment segment : region.getSegments()) {
        for (int column = segment.from(); column <= segment.to(); column++) {
          areaByCell.put((long) segment.row() * layout.getColumns() + column, region.getAreaId());
        }
      }
    }
    return areaByCell;
  }

  private Map<Long, Table> loadTables(List<FloorTablePlacement> placements) {
    Set<Long> ids =
        placements.stream()
            .filter(placement -> placement != null && placement.getTableId() != null)
            .map(FloorTablePlacement::getTableId)
            .collect(Collectors.toSet());
    return tableRepositoryPort.findAll().stream()
        .filter(table -> table.getId() != null && ids.contains(table.getId()))
        .collect(Collectors.toMap(Table::getId, table -> table));
  }

  private Map<Long, Area> loadAreas(Map<Long, Long> areaByCell) {
    Set<Long> ids = new HashSet<>(areaByCell.values());
    if (ids.isEmpty()) {
      return Map.of();
    }
    return areaRepositoryPort.findByIdIn(List.copyOf(ids)).stream()
        .collect(Collectors.toMap(Area::getId, area -> area));
  }
}
