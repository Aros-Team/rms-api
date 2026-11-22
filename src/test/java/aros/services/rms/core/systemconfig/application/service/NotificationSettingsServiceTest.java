/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.systemconfig.domain.NotificationSettings;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.output.NotificationPreferencesPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link NotificationSettingsService}.
 *
 * <p><b>Feature:</b> global notification preferences.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Returns the persisted preferences or documented defaults
 *   <li>Persists the full replacement payload
 *   <li>Rejects a null payload without writing
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the notification preferences port
 *   <li>Asserts the saved values and the null guard
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class NotificationSettingsServiceTest {

  @Mock private NotificationPreferencesPort notificationPreferencesPort;

  private NotificationSettingsService service;

  @BeforeEach
  void setUp() {
    service = new NotificationSettingsService(notificationPreferencesPort);
  }

  @Test
  void should_return_stored_preferences() {
    NotificationSettings stored = new NotificationSettings(false, true, false, true, true);
    when(notificationPreferencesPort.find()).thenReturn(Optional.of(stored));

    assertEquals(stored, service.get());
  }

  @Test
  void should_return_defaults_when_missing() {
    when(notificationPreferencesPort.find()).thenReturn(Optional.empty());

    NotificationSettings result = service.get();

    assertEquals(true, result.kitchenSoundEnabled());
    assertEquals(false, result.kitchenHapticsEnabled());
    assertEquals(true, result.lowStockAlertsEnabled());
  }

  @Test
  void should_persist_full_replacement() {
    NotificationSettings settings = new NotificationSettings(false, false, false, false, true);
    when(notificationPreferencesPort.save(any())).thenReturn(settings);

    assertEquals(settings, service.update(settings));

    verify(notificationPreferencesPort).save(settings);
  }

  @Test
  void should_reject_null_payload() {
    assertThrows(InvalidConfigurationException.class, () -> service.update(null));

    verify(notificationPreferencesPort, never()).save(any());
  }
}
