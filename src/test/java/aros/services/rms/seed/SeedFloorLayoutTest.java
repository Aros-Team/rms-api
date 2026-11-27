/* (C) 2026 */

package aros.services.rms.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for {@link SeedRunner}'s seeded floor-layout resource. */
class SeedFloorLayoutTest {

  @Test
  void shouldLoadTheCurrentRestaurantDistribution() {
    SeedRunner.FloorLayoutPlan plan = SeedRunner.loadFloorLayoutPlan();

    assertEquals(40, plan.columns());
    assertEquals(26, plan.rows());
    assertEquals(4, plan.regions().size());
    assertEquals(20, plan.tables().size());
  }

  @Test
  void shouldNotOverlapAreasAndPlaceEveryTableInServedCell() {
    SeedRunner.FloorLayoutPlan plan = SeedRunner.loadFloorLayoutPlan();
    Map<String, Integer> cellAreas = new HashMap<>();
    for (SeedRunner.FloorRegionSpec region : plan.regions()) {
      for (SeedRunner.FloorSegmentSpec segment : region.segments()) {
        for (int column = segment.from(); column <= segment.to(); column++) {
          String cell = segment.row() + ":" + column;
          assertTrue(cellAreas.put(cell, region.areaId()) == null, "overlapping cell " + cell);
        }
      }
    }

    Set<String> tableCells = new HashSet<>();
    for (SeedRunner.FloorTableSpec table : plan.tables()) {
      String cell = table.row() + ":" + table.column();
      assertTrue(tableCells.add(cell), "duplicate table cell " + cell);
      Integer areaId = cellAreas.get(cell);
      assertTrue(
          areaId != null && (areaId == 2 || areaId == 3),
          "table " + table.tableNumber() + " is not on a served cell");
    }
  }

  @Test
  void shouldEncodeSegmentsAsCompactRowRuns() {
    assertEquals(
        "[{\"row\":8,\"from\":15,\"to\":18},{\"row\":9,\"from\":15,\"to\":18}]",
        SeedRunner.segmentsJson(
            List.of(
                new SeedRunner.FloorSegmentSpec(8, 15, 18),
                new SeedRunner.FloorSegmentSpec(9, 15, 18))));
  }
}
