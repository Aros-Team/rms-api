/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.LaborCostMode;
import aros.services.rms.core.systemconfig.domain.LaborSettings;
import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.GetSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link LaborSettingsService}.
 *
 * <p><b>Feature:</b> typed labor costing settings.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Defaults to AUTO, a zero rate and 160 hours when no entry exists
 *   <li>Reads the persisted mode, rate and expected hours
 *   <li>Persists the three canonical keys on update
 *   <li>Rejects negative rates and impossible expected hours
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the configuration read/upsert use cases and the currency provider
 *   <li>Captures the three saved entries
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class LaborSettingsServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private GetSystemConfigurationUseCase getSystemConfigurationUseCase;
  @Mock private UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;
  @Mock private CurrencyProvider currencyProvider;

  private LaborSettingsService service;

  @BeforeEach
  void setUp() {
    service =
        new LaborSettingsService(
            getSystemConfigurationUseCase, updateSystemConfigurationUseCase, currencyProvider);
  }

  @Test
  void should_return_defaults_when_no_entries_exist() {
    when(getSystemConfigurationUseCase.findByKey(any())).thenReturn(Optional.empty());
    when(currencyProvider.getCurrency()).thenReturn(COP);

    LaborSettings result = service.get();

    assertEquals(LaborCostMode.AUTO, result.mode());
    assertEquals(new BigDecimal("0.00"), result.standardHourlyRate().amount());
    assertEquals(160, result.defaultExpectedHoursPerMonth());
  }

  @Test
  void should_read_persisted_values() {
    when(getSystemConfigurationUseCase.findByKey(LaborSettingsService.MODE_KEY))
        .thenReturn(Optional.of(entry("STANDARD")));
    when(getSystemConfigurationUseCase.findByKey(LaborSettingsService.STANDARD_RATE_KEY))
        .thenReturn(Optional.of(entry("12000.00")));
    when(getSystemConfigurationUseCase.findByKey(LaborSettingsService.EXPECTED_HOURS_KEY))
        .thenReturn(Optional.of(entry("180")));
    when(currencyProvider.getCurrency()).thenReturn(COP);

    LaborSettings result = service.get();

    assertEquals(LaborCostMode.STANDARD, result.mode());
    assertEquals(new BigDecimal("12000.00"), result.standardHourlyRate().amount());
    assertEquals(180, result.defaultExpectedHoursPerMonth());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_persist_three_canonical_keys() {
    when(getSystemConfigurationUseCase.findByKey(any())).thenReturn(Optional.empty());
    when(currencyProvider.getCurrency()).thenReturn(COP);
    LaborSettings settings =
        new LaborSettings(LaborCostMode.REAL, new Money(new BigDecimal("50"), COP), 160);

    service.update(settings);

    ArgumentCaptor<List<SystemConfiguration>> captor = ArgumentCaptor.forClass(List.class);
    verify(updateSystemConfigurationUseCase).saveAll(captor.capture());
    List<SystemConfiguration> saved = captor.getValue();
    assertEquals(3, saved.size());
    assertEquals("REAL", saved.get(0).getValue());
    assertEquals("50.00", saved.get(1).getValue());
    assertEquals("160", saved.get(2).getValue());
  }

  @Test
  void should_reject_negative_rate() {
    LaborSettings settings =
        new LaborSettings(LaborCostMode.STANDARD, new Money(new BigDecimal("-1"), COP), 160);

    assertThrows(InvalidConfigurationException.class, () -> service.update(settings));

    verify(updateSystemConfigurationUseCase, never()).saveAll(anyList());
  }

  @Test
  void should_reject_impossible_expected_hours() {
    LaborSettings settings =
        new LaborSettings(LaborCostMode.AUTO, new Money(BigDecimal.ZERO, COP), 1000);

    assertThrows(InvalidConfigurationException.class, () -> service.update(settings));

    verify(updateSystemConfigurationUseCase, never()).saveAll(anyList());
  }

  private static SystemConfiguration entry(String value) {
    return SystemConfiguration.builder().value(value).build();
  }
}
