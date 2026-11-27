/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SystemConfigurationMapper} and {@link SystemConfigurationGroupEntity}.
 *
 * <p><b>Feature:</b> Mapping between configuration group domain objects and their JPA entities,
 * including the server-managed creation timestamp.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Domain to entity mapping copies id, name, description and sortOrder
 *   <li>The mapper leaves {@code createdAt} null because the entity lifecycle callback owns it
 *   <li>The entity {@code @PrePersist} callback sets {@code createdAt} to the current instant
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Instantiates the generated MapStruct implementation directly, as neighbouring mapper tests
 *       do
 *   <li>Asserts every mapped field with exact values and checks the ignored timestamp stays null
 *   <li>Invokes the package-visible lifecycle callback directly and asserts the timestamp falls
 *       inside the instant range recorded around the call, so no wall-clock assumption is made
 * </ul>
 */
class SystemConfigurationMapperTest {

  private final SystemConfigurationMapper mapper = new SystemConfigurationMapperImpl();

  @Test
  void should_map_group_domain_to_entity_without_created_at() {
    SystemConfigurationGroup domain =
        SystemConfigurationGroup.builder()
            .id(7L)
            .name("GENERAL")
            .description("General settings")
            .sortOrder(3)
            .build();

    SystemConfigurationGroupEntity entity = mapper.toEntityGroup(domain);

    assertNotNull(entity);
    assertEquals(7L, entity.getId());
    assertEquals("GENERAL", entity.getName());
    assertEquals("General settings", entity.getDescription());
    assertEquals(3, entity.getSortOrder());
    assertNull(entity.getCreatedAt());
  }

  @Test
  void should_set_created_at_when_pre_persist_callback_runs() {
    SystemConfigurationGroupEntity entity = new SystemConfigurationGroupEntity();
    entity.setName("GENERAL");
    entity.setSortOrder(1);

    Instant before = Instant.now();
    entity.onCreate();
    Instant after = Instant.now();

    Instant createdAt = entity.getCreatedAt();
    assertNotNull(createdAt);
    assertTrue(
        !createdAt.isBefore(before) && !createdAt.isAfter(after),
        "createdAt should be between " + before + " and " + after + " pero fue " + createdAt);
  }
}
