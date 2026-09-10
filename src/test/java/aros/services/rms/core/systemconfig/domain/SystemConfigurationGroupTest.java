/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link SystemConfigurationGroup} domain record. */
class SystemConfigurationGroupTest {

  // ---------------------------------------------------------------------------
  // UC-01: should_create_group_with_all_fields
  // ---------------------------------------------------------------------------

  @Test
  void should_create_group_with_all_fields() {
    SystemConfigurationGroup group =
        SystemConfigurationGroup.builder()
            .id(1L)
            .name("General")
            .description("Moneda, zona horaria, pais")
            .sortOrder(1)
            .build();

    assertEquals(1L, group.getId());
    assertEquals("General", group.getName());
    assertEquals("Moneda, zona horaria, pais", group.getDescription());
    assertEquals(1, group.getSortOrder());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_allow_null_optional_fields
  // ---------------------------------------------------------------------------

  @Test
  void should_allow_null_optional_fields() {
    SystemConfigurationGroup group = SystemConfigurationGroup.builder().name("Payments").build();

    assertNull(group.getId());
    assertNull(group.getDescription());
    assertNull(group.getSortOrder());
  }
}
