/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import aros.services.rms.core.systemconfig.domain.CurrencySettings;
import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.event.CurrencyChangedEvent;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.GetCurrencySettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateCurrencySettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.util.Currency;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

/** Reads and updates the single source of truth for the system currency. */
@RequiredArgsConstructor
public class CurrencySettingsService
    implements GetCurrencySettingsUseCase, UpdateCurrencySettingsUseCase {

  /** Configuration key persisted by this service. */
  public static final String CURRENCY_KEY = "default_currency";

  private final CurrencyProvider currencyProvider;
  private final UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;
  private final ApplicationEventPublisher eventPublisher;

  /** {@inheritDoc} */
  @Override
  public CurrencySettings get() {
    return CurrencySettings.from(currencyProvider.getCurrency());
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CurrencySettings update(String code) {
    Currency currency = parse(code);
    updateSystemConfigurationUseCase.save(
        SystemConfiguration.builder()
            .key(CURRENCY_KEY)
            .value(currency.getCurrencyCode())
            .description("Moneda del sistema")
            .build());
    eventPublisher.publishEvent(new CurrencyChangedEvent(currency));
    return CurrencySettings.from(currency);
  }

  private Currency parse(String code) {
    if (code == null || code.isBlank()) {
      throw new InvalidConfigurationException("El código de moneda es obligatorio");
    }
    try {
      return Currency.getInstance(code.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new InvalidConfigurationException("Código de moneda ISO 4217 inválido: " + code);
    }
  }
}
