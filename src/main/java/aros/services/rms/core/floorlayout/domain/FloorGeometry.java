/* (C) 2026 */

package aros.services.rms.core.floorlayout.domain;

import aros.services.rms.core.floorlayout.domain.exception.InvalidFloorLayoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Pure geometry rules for the floor layout.
 *
 * <p>This class deliberately has no framework imports. It is the backend counterpart of the
 * frontend geometry utilities and follows the same canonical contract: zero-based coordinates,
 * inclusive {@code from}/{@code to} bounds and maximal row runs after normalization.
 */
public final class FloorGeometry {

  private FloorGeometry() {}

  /**
   * Normalizes a region's segments.
   *
   * <p>The result is sorted by row and column. Segments that overlap or touch on the same row are
   * merged into maximal runs. Duplicate runs therefore have no effect.
   *
   * @param segments submitted segments
   * @return canonical immutable segment list
   * @throws InvalidFloorLayoutException if a segment is null or has invalid local bounds
   */
  public static List<FloorSegment> normalize(List<FloorSegment> segments) {
    if (segments == null || segments.isEmpty()) {
      return List.of();
    }

    Map<Integer, List<FloorSegment>> byRow = new TreeMap<>();
    for (FloorSegment segment : segments) {
      if (segment == null) {
        throw new InvalidFloorLayoutException("La geometría contiene un segmento vacío");
      }
      if (segment.from() > segment.to()) {
        throw new InvalidFloorLayoutException(
            "Segmento inválido: fila "
                + segment.row()
                + ", columnas "
                + segment.from()
                + " a "
                + segment.to());
      }
      byRow.computeIfAbsent(segment.row(), ignored -> new ArrayList<>()).add(segment);
    }

    List<FloorSegment> normalized = new ArrayList<>();
    for (Map.Entry<Integer, List<FloorSegment>> entry : byRow.entrySet()) {
      List<FloorSegment> rowSegments = entry.getValue();
      rowSegments.sort((left, right) -> Integer.compare(left.from(), right.from()));

      int from = rowSegments.get(0).from();
      int to = rowSegments.get(0).to();
      for (int index = 1; index < rowSegments.size(); index++) {
        FloorSegment next = rowSegments.get(index);
        if (next.from() <= to + 1) {
          to = Math.max(to, next.to());
        } else {
          normalized.add(new FloorSegment(entry.getKey(), from, to));
          from = next.from();
          to = next.to();
        }
      }
      normalized.add(new FloorSegment(entry.getKey(), from, to));
    }
    return List.copyOf(normalized);
  }

  /**
   * Validates the complete region set against the grid dimensions.
   *
   * @param regions normalized regions
   * @param columns grid width
   * @param rows grid height
   * @throws InvalidFloorLayoutException when a key is duplicated, a region is empty or out of
   *     bounds, or two regions share a cell
   */
  public static void validateRegions(List<FloorRegion> regions, int columns, int rows) {
    if (regions == null) {
      throw new InvalidFloorLayoutException("La lista de regiones es obligatoria");
    }

    Set<String> keys = new HashSet<>();
    Set<String> occupied = new HashSet<>();

    for (FloorRegion region : regions) {
      if (region == null) {
        throw new InvalidFloorLayoutException("La distribución contiene una región vacía");
      }
      String key = region.getKey();
      if (key == null || key.isBlank()) {
        throw new InvalidFloorLayoutException("Cada región debe tener un identificador");
      }
      if (key.length() > FloorLayoutLimits.MAX_REGION_KEY_LENGTH) {
        throw new InvalidFloorLayoutException("El identificador de una región es demasiado largo");
      }
      if (!keys.add(key)) {
        throw new InvalidFloorLayoutException("Hay regiones con el mismo identificador");
      }
      if (region.getAreaId() == null) {
        throw new InvalidFloorLayoutException("Cada región debe estar vinculada a un área");
      }
      if (region.getSegments() == null || region.getSegments().isEmpty()) {
        throw new InvalidFloorLayoutException("La región «" + key + "» no tiene celdas dibujadas");
      }

      for (FloorSegment segment : region.getSegments()) {
        validateBounds(segment, columns, rows);
        for (int column = segment.from(); column <= segment.to(); column++) {
          String index = segment.row() + ":" + column;
          if (!occupied.add(index)) {
            throw new InvalidFloorLayoutException(
                "Las regiones no pueden compartir celdas: fila "
                    + segment.row()
                    + ", columna "
                    + column);
          }
        }
      }
    }
  }

  /**
   * Checks that a segment lies inside the grid.
   *
   * @param segment segment to check
   * @param columns grid width
   * @param rows grid height
   * @throws InvalidFloorLayoutException when the segment is outside the grid
   */
  public static void validateBounds(FloorSegment segment, int columns, int rows) {
    if (segment == null) {
      throw new InvalidFloorLayoutException("La geometría contiene un segmento vacío");
    }
    boolean inside =
        segment.row() < rows && segment.to() < columns && segment.from() <= segment.to();
    if (!inside) {
      throw new InvalidFloorLayoutException(
          "Una región queda fuera del lienzo. Ajusta el tamaño o vuelve a dibujarla.");
    }
  }
}
