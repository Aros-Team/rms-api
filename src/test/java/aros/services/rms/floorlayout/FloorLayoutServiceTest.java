/* (C) 2026 */

package aros.services.rms.floorlayout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.floorlayout.application.service.FloorLayoutService;
import aros.services.rms.core.floorlayout.domain.FloorLayout;
import aros.services.rms.core.floorlayout.domain.FloorRegion;
import aros.services.rms.core.floorlayout.domain.FloorSegment;
import aros.services.rms.core.floorlayout.domain.FloorTablePlacement;
import aros.services.rms.core.floorlayout.domain.exception.InvalidFloorLayoutException;
import aros.services.rms.core.floorlayout.port.output.FloorLayoutRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests for {@link FloorLayoutService}. */
@ExtendWith(MockitoExtension.class)
class FloorLayoutServiceTest {

  @Mock private FloorLayoutRepositoryPort floorLayoutRepositoryPort;
  @Mock private AreaRepositoryPort areaRepositoryPort;
  @Mock private TableRepositoryPort tableRepositoryPort;

  private FloorLayoutService service;

  @BeforeEach
  void setUp() {
    service =
        new FloorLayoutService(floorLayoutRepositoryPort, areaRepositoryPort, tableRepositoryPort);
  }

  @Test
  void shouldReturnDefaultsWhenNothingIsStored() {
    when(floorLayoutRepositoryPort.find()).thenReturn(Optional.empty());

    FloorLayout layout = service.get();

    assertEquals(32, layout.getColumns());
    assertEquals(20, layout.getRows());
    assertEquals(List.of(), layout.getRegions());
  }

  @Test
  void shouldNormalizeSegmentsAndPersistTheCanonicalLayout() {
    when(areaRepositoryPort.findByIdIn(List.of(3L)))
        .thenReturn(List.of(Area.builder().id(3L).name("Cocina").build()));
    when(floorLayoutRepositoryPort.save(any(FloorLayout.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    FloorLayout saved =
        service.save(
            layout(
                List.of(
                    region(
                        "r1", 3L, List.of(new FloorSegment(1, 3, 4), new FloorSegment(1, 0, 2))))));

    ArgumentCaptor<FloorLayout> captor = ArgumentCaptor.forClass(FloorLayout.class);
    verify(floorLayoutRepositoryPort).save(captor.capture());
    assertEquals(
        List.of(new FloorSegment(1, 0, 4)), captor.getValue().getRegions().get(0).getSegments());
    assertEquals(1, saved.getRegions().size());
  }

  @Test
  void shouldRejectInvalidCanvasDimensions() {
    assertThrows(
        InvalidFloorLayoutException.class,
        () -> service.save(FloorLayout.builder().columns(4).rows(20).regions(List.of()).build()));
    verify(floorLayoutRepositoryPort, never()).save(any());
  }

  @Test
  void shouldRejectOverlappingRegionsBeforePersisting() {
    assertThrows(
        InvalidFloorLayoutException.class,
        () ->
            service.save(
                layout(
                    List.of(
                        region("a", 1L, List.of(new FloorSegment(0, 0, 2))),
                        region("b", 1L, List.of(new FloorSegment(0, 2, 4)))))));
    verify(floorLayoutRepositoryPort, never()).save(any());
  }

  @Test
  void shouldRejectUnknownAreasBeforePersisting() {
    when(areaRepositoryPort.findByIdIn(List.of(99L))).thenReturn(List.of());

    assertThrows(
        InvalidFloorLayoutException.class,
        () -> service.save(layout(List.of(region("a", 99L, List.of(new FloorSegment(0, 0, 2)))))));
    verify(floorLayoutRepositoryPort, never()).save(any());
  }

  @Test
  void shouldRejectRegionsWithoutCells() {
    assertThrows(
        InvalidFloorLayoutException.class,
        () -> service.save(layout(List.of(region("a", 1L, List.of())))));
    verify(floorLayoutRepositoryPort, never()).save(any());
  }

  @Test
  void shouldPlaceTableInsideEnabledServeArea() {
    Area serveArea =
        Area.builder()
            .id(1L)
            .name("Salón")
            .enabled(true)
            .activities(java.util.Set.of(aros.services.rms.core.area.domain.AreaActivity.SERVE))
            .build();
    when(floorLayoutRepositoryPort.find())
        .thenReturn(
            Optional.of(layout(List.of(region("a", 1L, List.of(new FloorSegment(2, 2, 5)))))));
    when(areaRepositoryPort.findByIdIn(List.of(1L))).thenReturn(List.of(serveArea));
    when(tableRepositoryPort.findAll())
        .thenReturn(List.of(Table.builder().id(7L).tableNumber(7).capacity(4).build()));
    when(floorLayoutRepositoryPort.saveTablePlacements(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    List<FloorTablePlacement> saved =
        service.saveTablePlacements(
            List.of(FloorTablePlacement.builder().tableId(7L).row(2).column(3).build()));

    assertEquals(1, saved.size());
    assertEquals(7L, saved.get(0).getTableId());
  }

  @Test
  void shouldRejectTableOutsideServeArea() {
    Area kitchen =
        Area.builder()
            .id(1L)
            .name("Cocina")
            .enabled(true)
            .activities(
                java.util.Set.of(aros.services.rms.core.area.domain.AreaActivity.PREPARATION))
            .build();
    when(floorLayoutRepositoryPort.find())
        .thenReturn(
            Optional.of(layout(List.of(region("a", 1L, List.of(new FloorSegment(2, 2, 5)))))));
    when(areaRepositoryPort.findByIdIn(List.of(1L))).thenReturn(List.of(kitchen));
    when(tableRepositoryPort.findAll())
        .thenReturn(List.of(Table.builder().id(7L).tableNumber(7).capacity(4).build()));

    assertThrows(
        aros.services.rms.core.floorlayout.domain.exception.InvalidFloorTablePlacementException
            .class,
        () ->
            service.saveTablePlacements(
                List.of(FloorTablePlacement.builder().tableId(7L).row(2).column(3).build())));
  }

  private static FloorLayout layout(List<FloorRegion> regions) {
    return FloorLayout.builder().columns(32).rows(20).regions(regions).build();
  }

  private static FloorRegion region(String key, Long areaId, List<FloorSegment> segments) {
    return FloorRegion.builder().key(key).areaId(areaId).segments(segments).build();
  }
}
