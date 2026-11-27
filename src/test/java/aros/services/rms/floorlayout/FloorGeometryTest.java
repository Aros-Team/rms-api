/* (C) 2026 */

package aros.services.rms.floorlayout;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import aros.services.rms.core.floorlayout.domain.FloorGeometry;
import aros.services.rms.core.floorlayout.domain.FloorRegion;
import aros.services.rms.core.floorlayout.domain.FloorSegment;
import aros.services.rms.core.floorlayout.domain.exception.InvalidFloorLayoutException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FloorGeometry}.
 *
 * <p><b>Feature:</b> canonical segment normalization and layout validation.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Merges adjacent and overlapping runs on the same row
 *   <li>Rejects invalid local bounds, out-of-canvas cells and shared cells
 *   <li>Allows several independent regions for the same area
 * </ul>
 */
class FloorGeometryTest {

  @Test
  void shouldMergeOverlappingAndAdjacentSegmentsOnTheSameRow() {
    List<FloorSegment> normalized =
        FloorGeometry.normalize(
            List.of(
                new FloorSegment(1, 4, 6),
                new FloorSegment(0, 0, 2),
                new FloorSegment(1, 2, 3),
                new FloorSegment(1, 7, 8)));

    assertEquals(List.of(new FloorSegment(0, 0, 2), new FloorSegment(1, 2, 8)), normalized);
  }

  @Test
  void shouldRejectSegmentsWithInvalidLocalBounds() {
    assertThrows(
        InvalidFloorLayoutException.class,
        () -> FloorGeometry.normalize(List.of(new FloorSegment(0, 4, 2))));
  }

  @Test
  void shouldRejectRegionsThatShareOneCell() {
    List<FloorRegion> regions =
        List.of(
            region("a", 1L, new FloorSegment(0, 0, 2)), region("b", 2L, new FloorSegment(0, 2, 4)));

    assertThrows(
        InvalidFloorLayoutException.class, () -> FloorGeometry.validateRegions(regions, 10, 10));
  }

  @Test
  void shouldRejectCellsOutsideTheCanvas() {
    List<FloorRegion> regions = List.of(region("a", 1L, new FloorSegment(0, 8, 10)));

    assertThrows(
        InvalidFloorLayoutException.class, () -> FloorGeometry.validateRegions(regions, 10, 10));
  }

  @Test
  void shouldAllowMultipleRegionsLinkedToTheSameArea() {
    List<FloorRegion> regions =
        List.of(
            region("a", 1L, new FloorSegment(0, 0, 1)), region("b", 1L, new FloorSegment(2, 0, 1)));

    assertDoesNotThrow(() -> FloorGeometry.validateRegions(regions, 10, 10));
  }

  private static FloorRegion region(String key, Long areaId, FloorSegment... segments) {
    return FloorRegion.builder().key(key).areaId(areaId).segments(List.of(segments)).build();
  }
}
