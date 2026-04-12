/* (C) 2026 */
package aros.services.rms.daymenu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.daymenu.application.service.UpdateDayMenuService;
import aros.services.rms.core.daymenu.domain.DayMenu;
import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import aros.services.rms.core.daymenu.port.output.DayMenuHistoryRepositoryPort;
import aros.services.rms.core.daymenu.port.output.DayMenuRepositoryPort;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateDayMenuUseCaseImplTest {

  @Mock private ProductRepositoryPort productRepositoryPort;
  @Mock private DayMenuRepositoryPort dayMenuRepositoryPort;
  @Mock private DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort;
  @Mock private Logger logger;

  private UpdateDayMenuService useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new UpdateDayMenuService(
            productRepositoryPort, dayMenuRepositoryPort, dayMenuHistoryRepositoryPort, logger);
  }

  @Test
  void shouldCreateFirstDayMenuWhenNoPreviousExists() {
    var product =
        Product.builder()
            .id(1L)
            .name("Menú Especial")
            .basePrice(15.0)
            .hasOptions(true)
            .active(true)
            .build();
    var saved =
        DayMenu.builder()
            .id(1L)
            .productId(1L)
            .productName("Menú Especial")
            .productBasePrice(15.0)
            .validFrom(LocalDateTime.now())
            .createdBy("admin")
            .build();

    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
    when(dayMenuRepositoryPort.findActive()).thenReturn(Optional.empty());
    when(dayMenuRepositoryPort.save(any(DayMenu.class))).thenReturn(saved);

    var result = useCase.update(1L, "admin");

    assertNotNull(result);
    assertEquals(1L, result.getProductId());
    assertEquals("admin", result.getCreatedBy());
    verify(dayMenuHistoryRepositoryPort, never()).save(any());
    verify(dayMenuRepositoryPort, never()).deleteActive();
  }

  @Test
  void shouldArchivePreviousAndCreateNewDayMenu() {
    var product =
        Product.builder()
            .id(2L)
            .name("Nuevo Menú")
            .basePrice(18.0)
            .hasOptions(true)
            .active(true)
            .build();
    var existing =
        DayMenu.builder()
            .id(1L)
            .productId(1L)
            .productName("Menú Anterior")
            .productBasePrice(12.0)
            .validFrom(LocalDateTime.now().minusDays(1))
            .createdBy("chef")
            .build();
    var saved =
        DayMenu.builder()
            .id(2L)
            .productId(2L)
            .productName("Nuevo Menú")
            .productBasePrice(18.0)
            .validFrom(LocalDateTime.now())
            .createdBy("admin")
            .build();

    when(productRepositoryPort.findById(2L)).thenReturn(Optional.of(product));
    when(dayMenuRepositoryPort.findActive()).thenReturn(Optional.of(existing));
    when(dayMenuHistoryRepositoryPort.save(any(DayMenuHistory.class)))
        .thenReturn(DayMenuHistory.builder().id(1L).build());
    when(dayMenuRepositoryPort.save(any(DayMenu.class))).thenReturn(saved);

    var result = useCase.update(2L, "admin");

    assertNotNull(result);
    assertEquals(2L, result.getProductId());
    verify(dayMenuHistoryRepositoryPort).save(any(DayMenuHistory.class));
    verify(dayMenuRepositoryPort).deleteActive();
  }

  @Test
  void shouldThrowProductNotFoundWhenProductDoesNotExist() {
    when(productRepositoryPort.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> useCase.update(99L, "admin"));
    verify(dayMenuRepositoryPort, never()).findActive();
  }

  @Test
  void shouldThrowProductNotFoundWhenProductIsInactive() {
    var inactive = Product.builder().id(5L).name("Inactivo").hasOptions(true).active(false).build();
    when(productRepositoryPort.findById(5L)).thenReturn(Optional.of(inactive));

    assertThrows(ProductNotFoundException.class, () -> useCase.update(5L, "admin"));
  }

  @Test
  void shouldAllowProductWithoutOptions() {
    var product =
        Product.builder()
            .id(3L)
            .name("Sin Opciones")
            .basePrice(10.0)
            .hasOptions(false)
            .active(true)
            .build();
    var saved =
        DayMenu.builder()
            .id(3L)
            .productId(3L)
            .productName("Sin Opciones")
            .productBasePrice(10.0)
            .validFrom(LocalDateTime.now())
            .createdBy("admin")
            .build();

    when(productRepositoryPort.findById(3L)).thenReturn(Optional.of(product));
    when(dayMenuRepositoryPort.findActive()).thenReturn(Optional.empty());
    when(dayMenuRepositoryPort.save(any(DayMenu.class))).thenReturn(saved);

    var result = useCase.update(3L, "admin");

    assertNotNull(result);
    assertEquals(3L, result.getProductId());
  }

  @Test
  void shouldNotPersistWhenHistorySaveFails() {
    var product =
        Product.builder()
            .id(2L)
            .name("Nuevo")
            .basePrice(18.0)
            .hasOptions(true)
            .active(true)
            .build();
    var existing =
        DayMenu.builder()
            .id(1L)
            .productId(1L)
            .productName("Anterior")
            .productBasePrice(12.0)
            .validFrom(LocalDateTime.now().minusDays(1))
            .createdBy("chef")
            .build();

    when(productRepositoryPort.findById(2L)).thenReturn(Optional.of(product));
    when(dayMenuRepositoryPort.findActive()).thenReturn(Optional.of(existing));
    when(dayMenuHistoryRepositoryPort.save(any(DayMenuHistory.class)))
        .thenThrow(new RuntimeException("DB error"));

    assertThrows(RuntimeException.class, () -> useCase.update(2L, "admin"));
    verify(dayMenuRepositoryPort, never()).deleteActive();
    verify(dayMenuRepositoryPort, never()).save(any());
  }
}
