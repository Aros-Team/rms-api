/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.output.SystemConfigurationPort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link UpdateSystemConfigurationService}. */
@ExtendWith(MockitoExtension.class)
class UpdateSystemConfigurationServiceTest {

  @Mock private SystemConfigurationPort systemConfigurationPort;

  private UpdateSystemConfigurationService service;

  @BeforeEach
  void setUp() {
    service = new UpdateSystemConfigurationService(systemConfigurationPort);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_save_configuration
  // ---------------------------------------------------------------------------

  @Test
  void should_save_configuration() {
    SystemConfiguration config =
        SystemConfiguration.builder().key("default_currency").value("COP").build();

    SystemConfiguration saved =
        SystemConfiguration.builder().id(1L).key("default_currency").value("COP").build();

    when(systemConfigurationPort.save(any())).thenReturn(saved);

    SystemConfiguration result = service.save(config);

    assertNotNull(result);
    assertEquals("default_currency", result.getKey());

    ArgumentCaptor<SystemConfiguration> captor = ArgumentCaptor.forClass(SystemConfiguration.class);
    verify(systemConfigurationPort).save(captor.capture());
    assertNotNull(captor.getValue().getUpdatedAt());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_reject_blank_key
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_blank_key() {
    SystemConfiguration config = SystemConfiguration.builder().key("").value("COP").build();

    InvalidConfigurationException ex =
        assertThrows(InvalidConfigurationException.class, () -> service.save(config));

    assertNotNull(ex);
    verify(systemConfigurationPort, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_reject_blank_value
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_blank_value() {
    SystemConfiguration config =
        SystemConfiguration.builder().key("default_currency").value(" ").build();

    InvalidConfigurationException ex =
        assertThrows(InvalidConfigurationException.class, () -> service.save(config));

    assertNotNull(ex);
    verify(systemConfigurationPort, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_save_all_configurations
  // ---------------------------------------------------------------------------

  @Test
  void should_save_all_configurations() {
    List<SystemConfiguration> configs =
        List.of(
            SystemConfiguration.builder().key("currency").value("COP").build(),
            SystemConfiguration.builder().key("timezone").value("COT").build());

    List<SystemConfiguration> saved =
        List.of(
            SystemConfiguration.builder().id(1L).key("currency").value("COP").build(),
            SystemConfiguration.builder().id(2L).key("timezone").value("COT").build());

    when(systemConfigurationPort.saveAll(any())).thenReturn(saved);

    List<SystemConfiguration> result = service.saveAll(configs);

    assertEquals(2, result.size());
    verify(systemConfigurationPort).saveAll(configs);
  }
}
