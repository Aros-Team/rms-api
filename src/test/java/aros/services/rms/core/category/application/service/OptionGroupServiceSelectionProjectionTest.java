/* (C) 2026 */

package aros.services.rms.core.category.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import aros.services.rms.core.category.port.output.OptionGroupRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests selection-type projection behavior in {@link OptionGroupService}. */
@ExtendWith(MockitoExtension.class)
class OptionGroupServiceSelectionProjectionTest {

  @Mock private OptionGroupRepositoryPort optionGroupRepositoryPort;
  @Mock private Logger logger;

  private OptionGroupService service;

  @BeforeEach
  void setUp() {
    service = new OptionGroupService(optionGroupRepositoryPort, logger);
  }

  @Test
  void should_load_selection_types_for_all_requested_categories() {
    List<Long> ids = List.of(1L, 2L);
    Map<Long, String> expected = Map.of(1L, "SINGLE_CHOICE", 2L, "ADD_ON");
    when(optionGroupRepositoryPort.loadSelectionTypesByIds(ids)).thenReturn(expected);

    Map<Long, String> result = service.loadSelectionTypesByIds(ids);

    assertEquals(expected, result);
    verify(optionGroupRepositoryPort).loadSelectionTypesByIds(ids);
  }

  @Test
  void should_return_empty_selection_projection_without_repository_call() {
    Map<Long, String> result = service.loadSelectionTypesByIds(List.of());

    assertEquals(Map.of(), result);
    verifyNoInteractions(optionGroupRepositoryPort);
  }
}
