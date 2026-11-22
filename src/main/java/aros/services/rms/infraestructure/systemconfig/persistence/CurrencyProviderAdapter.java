/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import aros.services.rms.core.common.money.domain.CurrencyContext;
import aros.services.rms.core.systemconfig.domain.event.CurrencyChangedEvent;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.util.Currency;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Adapter for CurrencyProvider that reads the currency from system configuration.
 *
 * <p>This is the single source of truth for monetary currency. It also installs the resolved
 * currency into {@link CurrencyContext} so domain defaults and wire DTO fallbacks follow the
 * configured setting instead of a hardcoded ISO code.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyProviderAdapter implements CurrencyProvider {

  private static final String DEFAULT_CURRENCY_KEY = "default_currency";

  private final SystemConfigurationJpaRepository configurationRepo;

  private volatile Currency cachedCurrency;

  /** Returns the current system currency, using a cached value when available. */
  @Override
  public Currency getCurrency() {
    if (cachedCurrency != null) {
      return cachedCurrency;
    }
    return loadCurrency();
  }

  /** Forces a refresh of the cached currency from the database. */
  public Currency refreshCurrency() {
    return loadCurrency();
  }

  /**
   * Refreshes the cache when the currency setting changes.
   *
   * @param event the change event published by the settings service
   */
  @EventListener
  public void onCurrencyChanged(CurrencyChangedEvent event) {
    if (event.currency() != null) {
      cachedCurrency = event.currency();
      CurrencyContext.set(event.currency());
    }
  }

  private Currency loadCurrency() {
    Optional<SystemConfigurationEntity> config = configurationRepo.findByKey(DEFAULT_CURRENCY_KEY);
    String code =
        config
            .map(SystemConfigurationEntity::getValue)
            .orElse(CurrencyContext.BOOTSTRAP_CURRENCY_CODE);
    try {
      cachedCurrency = Currency.getInstance(code);
    } catch (IllegalArgumentException e) {
      log.warn(
          "Invalid currency code '{}' in configuration, falling back to {}",
          code,
          CurrencyContext.BOOTSTRAP_CURRENCY_CODE);
      cachedCurrency = Currency.getInstance(CurrencyContext.BOOTSTRAP_CURRENCY_CODE);
    }
    CurrencyContext.set(cachedCurrency);
    return cachedCurrency;
  }
}
