/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.LaborCostMode;
import aros.services.rms.core.systemconfig.domain.LaborSettings;
import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.GetLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.GetSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Reads and updates the typed labor costing configuration. */
@RequiredArgsConstructor
public class LaborSettingsService implements GetLaborSettingsUseCase, UpdateLaborSettingsUseCase {

  /** Configuration key holding the costing mode. */
  public static final String MODE_KEY = "labor_cost_mode";

  /** Configuration key holding the fallback hourly rate. */
  public static final String STANDARD_RATE_KEY = "labor_standard_hourly_rate";

  /** Configuration key holding the fallback expected hours per month. */
  public static final String EXPECTED_HOURS_KEY = "labor_default_expected_hours_per_month";

  private static final int DEFAULT_EXPECTED_HOURS = 160;

  private final GetSystemConfigurationUseCase getSystemConfigurationUseCase;
  private final UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;
  private final CurrencyProvider currencyProvider;

  /** {@inheritDoc} */
  @Override
  public LaborSettings get() {
    LaborCostMode mode = LaborCostMode.from(findValue(MODE_KEY).orElse(LaborCostMode.AUTO.name()));
    Money standardRate =
        new Money(
            parseDecimal(findValue(STANDARD_RATE_KEY).orElse("0")), currencyProvider.getCurrency());
    int expectedHours =
        parseInt(findValue(EXPECTED_HOURS_KEY).orElse(String.valueOf(DEFAULT_EXPECTED_HOURS)));
    return new LaborSettings(mode, standardRate, expectedHours);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public LaborSettings update(LaborSettings settings) {
    if (settings == null || settings.mode() == null) {
      throw new InvalidConfigurationException("Labor cost mode is required");
    }
    if (settings.standardHourlyRate() == null || settings.standardHourlyRate().isNegative()) {
      throw new InvalidConfigurationException("standardHourlyRate", "negative or null");
    }
    if (settings.defaultExpectedHoursPerMonth() < 0
        || settings.defaultExpectedHoursPerMonth() > 744) {
      throw new InvalidConfigurationException(
          "defaultExpectedHoursPerMonth", String.valueOf(settings.defaultExpectedHoursPerMonth()));
    }
    updateSystemConfigurationUseCase.saveAll(
        List.of(
            entry(MODE_KEY, settings.mode().name(), "Modo de costo de mano de obra"),
            entry(
                STANDARD_RATE_KEY,
                settings.standardHourlyRate().amount().toPlainString(),
                "Tarifa horaria estándar de mano de obra"),
            entry(
                EXPECTED_HOURS_KEY,
                String.valueOf(settings.defaultExpectedHoursPerMonth()),
                "Horas mensuales esperadas por defecto")));
    return get();
  }

  private SystemConfiguration entry(String key, String value, String description) {
    return SystemConfiguration.builder().key(key).value(value).description(description).build();
  }

  private java.util.Optional<String> findValue(String key) {
    return getSystemConfigurationUseCase.findByKey(key).map(SystemConfiguration::getValue);
  }

  private BigDecimal parseDecimal(String value) {
    try {
      BigDecimal parsed = new BigDecimal(value.trim());
      if (parsed.signum() < 0) {
        throw new InvalidConfigurationException(STANDARD_RATE_KEY, value);
      }
      return parsed;
    } catch (NumberFormatException e) {
      throw new InvalidConfigurationException(STANDARD_RATE_KEY, value);
    }
  }

  private int parseInt(String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return DEFAULT_EXPECTED_HOURS;
    }
  }
}
