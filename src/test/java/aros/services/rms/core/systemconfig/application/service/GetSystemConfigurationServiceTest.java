/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import aros.services.rms.core.systemconfig.domain.port.output.SystemConfigurationPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link GetSystemConfigurationService}. */
@ExtendWith(MockitoExtension.class)
class GetSystemConfigurationServiceTest {

  @Mock private SystemConfigurationPort systemConfigurationPort;

  private GetSystemConfigurationService service;

  @BeforeEach
  void setUp() {
    service = new GetSystemConfigurationService(systemConfigurationPort);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_find_by_key
  // ---------------------------------------------------------------------------

  @Test
  void should_find_by_key() {
    SystemConfiguration config =
        SystemConfiguration.builder().id(1L).key("default_currency").value("COP").build();

    when(systemConfigurationPort.findByKey("default_currency")).thenReturn(Optional.of(config));

    Optional<SystemConfiguration> result = service.findByKey("default_currency");

    assertTrue(result.isPresent());
    assertEquals("default_currency", result.get().getKey());
    assertEquals("COP", result.get().getValue());
    verify(systemConfigurationPort).findByKey("default_currency");
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_return_empty_when_key_not_found
  // ---------------------------------------------------------------------------

  @Test
  void should_return_empty_when_key_not_found() {
    when(systemConfigurationPort.findByKey("nonexistent_key")).thenReturn(Optional.empty());

    Optional<SystemConfiguration> result = service.findByKey("nonexistent_key");

    assertFalse(result.isPresent());
    verify(systemConfigurationPort).findByKey("nonexistent_key");
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_find_by_group_id
  // ---------------------------------------------------------------------------

  @Test
  void should_find_by_group_id() {
    List<SystemConfiguration> configs =
        List.of(
            SystemConfiguration.builder().id(1L).key("currency").value("COP").groupId(1L).build(),
            SystemConfiguration.builder().id(2L).key("timezone").value("COT").groupId(1L).build());

    when(systemConfigurationPort.findByGroupId(1L)).thenReturn(configs);

    List<SystemConfiguration> result = service.findByGroupId(1L);

    assertEquals(2, result.size());
    assertEquals("currency", result.get(0).getKey());
    assertEquals("timezone", result.get(1).getKey());
    verify(systemConfigurationPort).findByGroupId(1L);
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_find_all_groups
  // ---------------------------------------------------------------------------

  @Test
  void should_find_all_groups() {
    List<SystemConfigurationGroup> groups =
        List.of(
            SystemConfigurationGroup.builder().id(1L).name("General").sortOrder(1).build(),
            SystemConfigurationGroup.builder().id(2L).name("Payments").sortOrder(2).build());

    when(systemConfigurationPort.findAllGroups()).thenReturn(groups);

    List<SystemConfigurationGroup> result = service.findAllGroups();

    assertEquals(2, result.size());
    assertEquals("General", result.get(0).getName());
    assertEquals("Payments", result.get(1).getName());
    verify(systemConfigurationPort).findAllGroups();
  }
}
