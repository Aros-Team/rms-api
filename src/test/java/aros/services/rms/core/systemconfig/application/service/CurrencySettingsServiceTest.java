/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.systemconfig.domain.CurrencySettings;
import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.event.CurrencyChangedEvent;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Tests for {@link CurrencySettingsService}.
 *
 * <p><b>Feature:</b> typed currency setting and its propagation.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Reads the currency from the single CurrencyProvider source
 *   <li>Accepts ISO 4217 codes case-insensitively and persists the canonical code
 *   <li>Rejects unknown codes without writing or publishing
 *   <li>Publishes a CurrencyChangedEvent so caches refresh immediately
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the provider, upsert use case and event publisher
 *   <li>Captures the persisted key/value and the published event
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class CurrencySettingsServiceTest {

  @Mock private CurrencyProvider currencyProvider;
  @Mock private UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;
  @Mock private ApplicationEventPublisher eventPublisher;

  private CurrencySettingsService service;

  @BeforeEach
  void setUp() {
    service =
        new CurrencySettingsService(
            currencyProvider, updateSystemConfigurationUseCase, eventPublisher);
  }

  @Test
  void should_read_currency_from_provider() {
    when(currencyProvider.getCurrency()).thenReturn(Currency.getInstance("COP"));

    CurrencySettings result = service.get();

    assertEquals("COP", result.code());
    assertEquals(2, result.defaultFractionDigits());
  }

  @Test
  void should_persist_canonical_iso_code_and_publish_event() {
    CurrencySettings result = service.update(" usd ");

    assertEquals("USD", result.code());
    ArgumentCaptor<SystemConfiguration> configCaptor =
        ArgumentCaptor.forClass(SystemConfiguration.class);
    verify(updateSystemConfigurationUseCase).save(configCaptor.capture());
    assertEquals("default_currency", configCaptor.getValue().getKey());
    assertEquals("USD", configCaptor.getValue().getValue());

    ArgumentCaptor<CurrencyChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(CurrencyChangedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertEquals("USD", eventCaptor.getValue().currency().getCurrencyCode());
  }

  @Test
  void should_reject_unknown_code() {
    assertThrows(InvalidConfigurationException.class, () -> service.update("ZZZ"));

    verify(updateSystemConfigurationUseCase, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }
}
