/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SystemConfiguration} domain record. */
class SystemConfigurationTest {

  // ---------------------------------------------------------------------------
  // UC-01: should_create_system_configuration_with_all_fields
  // ---------------------------------------------------------------------------

  @Test
  void should_create_system_configuration_with_all_fields() {
    Instant now = Instant.parse("2026-08-03T10:00:00Z");

    SystemConfiguration config =
        SystemConfiguration.builder()
            .id(1L)
            .key("default_currency")
            .value("COP")
            .description("Moneda del sistema")
            .groupId(1L)
            .sortOrder(1)
            .updatedBy(42L)
            .updatedAt(now)
            .build();

    assertEquals(1L, config.getId());
    assertEquals("default_currency", config.getKey());
    assertEquals("COP", config.getValue());
    assertEquals("Moneda del sistema", config.getDescription());
    assertEquals(1L, config.getGroupId());
    assertEquals(1, config.getSortOrder());
    assertEquals(42L, config.getUpdatedBy());
    assertEquals(now, config.getUpdatedAt());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_allow_null_optional_fields
  // ---------------------------------------------------------------------------

  @Test
  void should_allow_null_optional_fields() {
    SystemConfiguration config =
        SystemConfiguration.builder().key("timezone").value("America/Bogota").build();

    assertNull(config.getId());
    assertNull(config.getDescription());
    assertNull(config.getGroupId());
    assertNull(config.getSortOrder());
    assertNull(config.getUpdatedBy());
    assertNull(config.getUpdatedAt());
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_return_correct_values
  // ---------------------------------------------------------------------------

  @Test
  void should_return_correct_values() {
    SystemConfiguration config =
        SystemConfiguration.builder()
            .id(99L)
            .key("tax_rate")
            .value("19.0")
            .groupId(3L)
            .sortOrder(5)
            .build();

    assertEquals(99L, config.getId());
    assertEquals("tax_rate", config.getKey());
    assertEquals("19.0", config.getValue());
    assertEquals(3L, config.getGroupId());
    assertEquals(5, config.getSortOrder());
  }
}
