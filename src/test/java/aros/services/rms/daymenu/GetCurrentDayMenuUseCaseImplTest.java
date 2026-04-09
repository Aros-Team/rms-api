/* (C) 2026 */
package aros.services.rms.daymenu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import aros.services.rms.core.daymenu.application.service.GetCurrentDayMenuService;
import aros.services.rms.core.daymenu.domain.DayMenu;
import aros.services.rms.core.daymenu.port.output.DayMenuRepositoryPort;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCurrentDayMenuUseCaseImplTest {

  @Mock private DayMenuRepositoryPort dayMenuRepositoryPort;

  private GetCurrentDayMenuService useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetCurrentDayMenuService(dayMenuRepositoryPort);
  }

  @Test
  void shouldReturnActiveDayMenuWhenExists() {
    var dayMenu =
        DayMenu.builder()
            .id(1L)
            .productId(5L)
            .productName("Menú Especial")
            .productBasePrice(15.0)
            .validFrom(LocalDateTime.now())
            .createdBy("admin")
            .build();

    when(dayMenuRepositoryPort.findActive()).thenReturn(Optional.of(dayMenu));

    var result = useCase.getCurrent();

    assertTrue(result.isPresent());
    assertEquals(5L, result.get().getProductId());
    assertEquals("admin", result.get().getCreatedBy());
  }

  @Test
  void shouldReturnEmptyWhenNoDayMenuConfigured() {
    when(dayMenuRepositoryPort.findActive()).thenReturn(Optional.empty());

    var result = useCase.getCurrent();

    assertTrue(result.isEmpty());
  }
}
