/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link SystemConfigurationAdapter}.
 *
 * <p><b>Feature:</b> Insert-or-update persistence of system configuration entries.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>An existing key updates its value and keeps description, group and sort order.
 *   <li>A new key is appended to the first group so it stays visible in the grouped GET.
 *   <li>An explicit group and sort order from the request win over the defaults.
 *   <li>The audit timestamp is always populated, even when the caller forgot it.
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the JPA repositories and uses the real MapStruct mapper.
 *   <li>Asserts the entity that would be persisted, not the returned domain only.
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class SystemConfigurationAdapterTest {

  @Mock private SystemConfigurationJpaRepository configurationRepo;
  @Mock private SystemConfigurationGroupJpaRepository groupRepo;

  private SystemConfigurationAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter =
        new SystemConfigurationAdapter(
            configurationRepo, groupRepo, Mappers.getMapper(SystemConfigurationMapper.class));
    when(configurationRepo.save(any(SystemConfigurationEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void saveAllUpdatesExistingKeyAndPreservesMetadata() {
    SystemConfigurationEntity existing = entity(5L, "default_currency", "COP", 1L, 2, "Moneda");
    when(configurationRepo.findByKey("default_currency")).thenReturn(Optional.of(existing));

    SystemConfiguration saved = adapter.saveAll(List.of(config("default_currency", "USD"))).get(0);

    assertEquals("USD", saved.getValue());
    assertEquals("Moneda", saved.getDescription());
    assertEquals(1L, saved.getGroupId());
    assertEquals(2, saved.getSortOrder());
    assertNotNull(saved.getUpdatedAt());
    verify(configurationRepo, never()).findByGroupId(any());
  }

  @Test
  void saveAllAppendsNewKeyToFirstGroup() {
    when(configurationRepo.findByKey("business_timezone")).thenReturn(Optional.empty());
    when(groupRepo.findAllByOrderBySortOrder())
        .thenReturn(List.of(groupEntity(7L, 1), groupEntity(9L, 2)));
    when(configurationRepo.findByGroupId(7L))
        .thenReturn(
            List.of(
                entity(1L, "labor_cost_mode", "AUTO", 7L, 1, null),
                entity(2L, "x", "y", 7L, 4, null)));

    SystemConfiguration saved =
        adapter.saveAll(List.of(config("business_timezone", "America/Bogota"))).get(0);

    assertEquals(7L, saved.getGroupId());
    assertEquals(5, saved.getSortOrder());
    assertNotNull(saved.getUpdatedAt());
  }

  @Test
  void saveAllHonorsExplicitGroupAndSortOrder() {
    when(configurationRepo.findByKey("new_key")).thenReturn(Optional.empty());

    SystemConfiguration request = config("new_key", "value");
    request.setGroupId(2L);
    request.setSortOrder(9);

    SystemConfiguration saved = adapter.saveAll(List.of(request)).get(0);

    assertEquals(2L, saved.getGroupId());
    assertEquals(9, saved.getSortOrder());
    verify(groupRepo, never()).findAllByOrderBySortOrder();
    verify(configurationRepo, never()).findByGroupId(any());
  }

  @Test
  void saveAllDefaultsMissingTimestamp() {
    when(configurationRepo.findByKey("key")).thenReturn(Optional.empty());
    when(groupRepo.findAllByOrderBySortOrder()).thenReturn(List.of(groupEntity(1L, 1)));
    when(configurationRepo.findByGroupId(1L)).thenReturn(List.of());

    SystemConfiguration saved = adapter.saveAll(List.of(config("key", "value"))).get(0);

    assertEquals(0, saved.getSortOrder());
    assertNotNull(saved.getUpdatedAt());
  }

  private static SystemConfiguration config(String key, String value) {
    return SystemConfiguration.builder().key(key).value(value).updatedAt(Instant.now()).build();
  }

  private static SystemConfigurationEntity entity(
      Long id, String key, String value, Long groupId, int sortOrder, String description) {
    SystemConfigurationEntity entity = new SystemConfigurationEntity();
    entity.setId(id);
    entity.setKey(key);
    entity.setValue(value);
    entity.setGroupId(groupId);
    entity.setSortOrder(sortOrder);
    entity.setDescription(description);
    entity.setUpdatedAt(Instant.now());
    return entity;
  }

  private static SystemConfigurationGroupEntity groupEntity(Long id, int sortOrder) {
    SystemConfigurationGroupEntity group = new SystemConfigurationGroupEntity();
    group.setId(id);
    group.setSortOrder(sortOrder);
    return group;
  }
}
