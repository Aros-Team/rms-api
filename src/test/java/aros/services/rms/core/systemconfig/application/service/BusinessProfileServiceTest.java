/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.systemconfig.domain.BusinessProfile;
import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.BusinessProfilePort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link BusinessProfileService}.
 *
 * <p><b>Feature:</b> typed business profile settings.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Returns the persisted profile or documented defaults
 *   <li>Rejects blank names and invalid IANA timezones without saving
 *   <li>Keeps the legacy {@code business_timezone} entry in sync when saving
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the profile port and the configuration upsert use case
 *   <li>Captures the entry written to the configuration port
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class BusinessProfileServiceTest {

  @Mock private BusinessProfilePort businessProfilePort;
  @Mock private UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;

  private BusinessProfileService service;

  @BeforeEach
  void setUp() {
    service = new BusinessProfileService(businessProfilePort, updateSystemConfigurationUseCase);
  }

  @Test
  void should_return_stored_profile() {
    BusinessProfile stored =
        BusinessProfile.builder().restaurantName("Rincón").timezone("America/Bogota").build();
    when(businessProfilePort.find()).thenReturn(Optional.of(stored));

    BusinessProfile result = service.get();

    assertEquals("Rincón", result.getRestaurantName());
  }

  @Test
  void should_return_defaults_when_profile_is_missing() {
    when(businessProfilePort.find()).thenReturn(Optional.empty());

    BusinessProfile result = service.get();

    assertEquals("RMS", result.getRestaurantName());
    assertEquals("America/Bogota", result.getTimezone());
  }

  @Test
  void should_save_profile_and_sync_legacy_timezone_entry() {
    BusinessProfile profile =
        BusinessProfile.builder()
            .restaurantName("Rincón")
            .timezone("America/Bogota")
            .locale("es-CO")
            .build();
    when(businessProfilePort.save(any())).thenReturn(profile);

    service.update(profile);

    verify(businessProfilePort).save(profile);
    ArgumentCaptor<SystemConfiguration> captor = ArgumentCaptor.forClass(SystemConfiguration.class);
    verify(updateSystemConfigurationUseCase).save(captor.capture());
    assertEquals("business_timezone", captor.getValue().getKey());
    assertEquals("America/Bogota", captor.getValue().getValue());
  }

  @Test
  void should_reject_invalid_timezone_without_saving() {
    BusinessProfile profile =
        BusinessProfile.builder()
            .restaurantName("Rincón")
            .timezone("Mars/Olympus")
            .locale("es-CO")
            .build();

    assertThrows(InvalidConfigurationException.class, () -> service.update(profile));

    verify(businessProfilePort, never()).save(any());
  }

  @Test
  void should_reject_blank_restaurant_name() {
    BusinessProfile profile =
        BusinessProfile.builder().restaurantName(" ").timezone("America/Bogota").build();

    assertThrows(InvalidConfigurationException.class, () -> service.update(profile));

    verify(businessProfilePort, never()).save(any());
  }
}
