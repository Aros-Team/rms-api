/* (C) 2026 */

package aros.services.rms.core.category.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests selection-type projection behavior in {@link OptionCategoryService}. */
@ExtendWith(MockitoExtension.class)
class OptionCategoryServiceSelectionProjectionTest {

  @Mock private OptionCategoryRepositoryPort optionCategoryRepositoryPort;
  @Mock private Logger logger;

  private OptionCategoryService service;

  @BeforeEach
  void setUp() {
    service = new OptionCategoryService(optionCategoryRepositoryPort, logger);
  }

  @Test
  void should_load_selection_types_for_all_requested_categories() {
    List<Long> ids = List.of(1L, 2L);
    Map<Long, String> expected = Map.of(1L, "SINGLE_CHOICE", 2L, "EXTRA");
    when(optionCategoryRepositoryPort.loadSelectionTypesByIds(ids)).thenReturn(expected);

    Map<Long, String> result = service.loadSelectionTypesByIds(ids);

    assertEquals(expected, result);
    verify(optionCategoryRepositoryPort).loadSelectionTypesByIds(ids);
  }

  @Test
  void should_return_empty_selection_projection_without_repository_call() {
    Map<Long, String> result = service.loadSelectionTypesByIds(List.of());

    assertEquals(Map.of(), result);
    verifyNoInteractions(optionCategoryRepositoryPort);
  }
}
