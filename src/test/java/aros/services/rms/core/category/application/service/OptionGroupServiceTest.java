/* (C) 2026 */

package aros.services.rms.core.category.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.category.application.exception.OptionGroupNotFoundException;
import aros.services.rms.core.category.application.exception.OptionGroupRequiresProductException;
import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.port.output.OptionGroupRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for OptionGroupService business rules and CRUD operations. */
@ExtendWith(MockitoExtension.class)
class OptionGroupServiceTest {

  @Mock private OptionGroupRepositoryPort optionGroupRepositoryPort;
  @Mock private Logger logger;

  private OptionGroupService service;

  @BeforeEach
  void setUp() {
    service = new OptionGroupService(optionGroupRepositoryPort, logger);
  }

  // ---------------------------------------------------------------------------
  // Business rule: OptionGroupRequiresProductException
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowOptionGroupRequiresProductException_whenProductIdsEmpty() {
    OptionGroup group = OptionGroup.builder().name("Test").description("Desc").build();

    assertThrows(
        OptionGroupRequiresProductException.class, () -> service.create(group, List.of(), false));
  }

  @Test
  void shouldThrowOptionGroupRequiresProductException_whenProductIdsNull() {
    OptionGroup group = OptionGroup.builder().name("Test").description("Desc").build();

    assertThrows(
        OptionGroupRequiresProductException.class, () -> service.create(group, null, false));
  }

  @Test
  void shouldThrowOnUpdate_whenProductIdsEmpty() {
    OptionGroup updated = OptionGroup.builder().name("Updated").description("Desc").build();

    assertThrows(
        OptionGroupRequiresProductException.class,
        () -> service.update(1L, updated, List.of(), false));
  }

  // ---------------------------------------------------------------------------
  // Create happy path
  // ---------------------------------------------------------------------------

  @Test
  void shouldCreateOptionGroup_whenValidProductIdsProvided() {
    OptionGroup input =
        OptionGroup.builder().name("Proteína").description("Elección de proteína").build();
    OptionGroup saved =
        OptionGroup.builder().id(1L).name("Proteína").description("Elección de proteína").build();
    when(optionGroupRepositoryPort.save(any())).thenReturn(saved);

    OptionGroup result = service.create(input, List.of(1L, 2L), true);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(optionGroupRepositoryPort).save(input);
    verify(optionGroupRepositoryPort).replaceProductAssociations(1L, List.of(1L, 2L), true);
  }

  // ---------------------------------------------------------------------------
  // Update happy path
  // ---------------------------------------------------------------------------

  @Test
  void shouldUpdateOptionGroup_andReplaceAssociations() {
    OptionGroup existing = OptionGroup.builder().id(1L).name("Old").description("Old desc").build();
    when(optionGroupRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
    when(optionGroupRepositoryPort.save(any())).thenReturn(existing);

    OptionGroup updated = OptionGroup.builder().name("New").description("New desc").build();

    OptionGroup result = service.update(1L, updated, List.of(3L), false);

    assertNotNull(result);
    assertEquals("New", result.getName());
    verify(optionGroupRepositoryPort).replaceProductAssociations(1L, List.of(3L), false);
  }

  // ---------------------------------------------------------------------------
  // FindById not found
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowOptionGroupNotFoundException_whenNotFound() {
    when(optionGroupRepositoryPort.findById(99L)).thenReturn(Optional.empty());

    assertThrows(OptionGroupNotFoundException.class, () -> service.findById(99L));
  }

  // ---------------------------------------------------------------------------
  // findByProductId
  // ---------------------------------------------------------------------------

  @Test
  void shouldFindByProductId() {
    OptionGroup g1 = OptionGroup.builder().id(1L).name("Group1").build();
    OptionGroup g2 = OptionGroup.builder().id(2L).name("Group2").build();
    when(optionGroupRepositoryPort.findByProductId(5L)).thenReturn(List.of(g1, g2));

    List<OptionGroup> result = service.findByProductId(5L);

    assertEquals(2, result.size());
    verify(optionGroupRepositoryPort).findByProductId(5L);
  }

  // ---------------------------------------------------------------------------
  // loadProductIdsByOptionGroupIds
  // ---------------------------------------------------------------------------

  @Test
  void shouldLoadProductIdsByOptionGroupIds() {
    when(optionGroupRepositoryPort.loadProductIdsByOptionGroupIds(List.of(1L, 2L)))
        .thenReturn(Map.of(1L, List.of(10L, 20L), 2L, List.of(30L)));

    Map<Long, List<Long>> result = service.loadProductIdsByOptionGroupIds(List.of(1L, 2L));

    assertEquals(2, result.size());
    assertEquals(List.of(10L, 20L), result.get(1L));
    assertEquals(List.of(30L), result.get(2L));
  }

  @Test
  void shouldReturnEmptyMap_whenIdsEmpty() {
    assertEquals(Map.of(), service.loadProductIdsByOptionGroupIds(List.of()));
    assertEquals(Map.of(), service.loadProductIdsByOptionGroupIds(null));
  }
}
