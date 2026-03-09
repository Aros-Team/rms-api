/* (C) 2026 */
package aros.services.rms.area;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.area.application.exception.AreaAlreadyExistsException;
import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.application.usecases.AreaUseCaseImpl;
import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.domain.AreaType;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AreaUseCaseImplTest {

  @Mock private AreaRepositoryPort areaRepositoryPort;
  @Mock private Logger logger;

  private AreaUseCaseImpl areaUseCase;

  @BeforeEach
  void setUp() {
    areaUseCase = new AreaUseCaseImpl(areaRepositoryPort, logger);
  }

  @Test
  void shouldCreateAreaSuccessfully() {
    Area area = Area.builder().name("Kitchen").type(AreaType.KITCHEN).build();
    Area saved = Area.builder().id(1L).name("Kitchen").type(AreaType.KITCHEN).enabled(true).build();

    when(areaRepositoryPort.findByName("Kitchen")).thenReturn(Optional.empty());
    when(areaRepositoryPort.save(any(Area.class))).thenReturn(saved);

    Area result = areaUseCase.create(area);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Kitchen", result.getName());
    assertTrue(result.isEnabled());
    verify(areaRepositoryPort).save(any(Area.class));
  }

  @Test
  void shouldThrowWhenAreaNameAlreadyExists() {
    Area area = Area.builder().name("Kitchen").type(AreaType.KITCHEN).build();
    Area existing = Area.builder().id(1L).name("Kitchen").build();

    when(areaRepositoryPort.findByName("Kitchen")).thenReturn(Optional.of(existing));

    assertThrows(AreaAlreadyExistsException.class, () -> areaUseCase.create(area));
  }

  @Test
  void shouldUpdateAreaSuccessfully() {
    Area existing =
        Area.builder().id(1L).name("Kitchen").type(AreaType.KITCHEN).enabled(true).build();
    Area updateData = Area.builder().name("Bar").type(AreaType.BARTENDER).build();
    Area saved = Area.builder().id(1L).name("Bar").type(AreaType.BARTENDER).enabled(true).build();

    when(areaRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
    when(areaRepositoryPort.save(any(Area.class))).thenReturn(saved);

    Area result = areaUseCase.update(1L, updateData);

    assertEquals("Bar", result.getName());
    assertEquals(AreaType.BARTENDER, result.getType());
  }

  @Test
  void shouldThrowWhenUpdatingNonExistentArea() {
    when(areaRepositoryPort.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        AreaNotFoundException.class,
        () -> areaUseCase.update(99L, Area.builder().name("Test").build()));
  }

  @Test
  void shouldFindAllAreas() {
    List<Area> areas =
        List.of(
            Area.builder().id(1L).name("Kitchen").build(),
            Area.builder().id(2L).name("Bar").build());

    when(areaRepositoryPort.findAll()).thenReturn(areas);

    List<Area> result = areaUseCase.findAll();

    assertEquals(2, result.size());
  }

  @Test
  void shouldFindAreaById() {
    Area area = Area.builder().id(1L).name("Kitchen").build();

    when(areaRepositoryPort.findById(1L)).thenReturn(Optional.of(area));

    Area result = areaUseCase.findById(1L);

    assertEquals(1L, result.getId());
  }

  @Test
  void shouldThrowWhenFindingNonExistentArea() {
    when(areaRepositoryPort.findById(99L)).thenReturn(Optional.empty());

    assertThrows(AreaNotFoundException.class, () -> areaUseCase.findById(99L));
  }

  @Test
  void shouldToggleEnabledStatus() {
    Area existing = Area.builder().id(1L).name("Kitchen").enabled(true).build();
    Area saved = Area.builder().id(1L).name("Kitchen").enabled(false).build();

    when(areaRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
    when(areaRepositoryPort.save(any(Area.class))).thenReturn(saved);

    Area result = areaUseCase.toggleEnabled(1L);

    assertFalse(result.isEnabled());
  }
}
