/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.util.Currency;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Adapter for CurrencyProvider that reads the currency from system configuration. */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyProviderAdapter implements CurrencyProvider {

  private static final String DEFAULT_CURRENCY_KEY = "default_currency";
  private static final String FALLBACK_CURRENCY_CODE = "COP";

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

  private Currency loadCurrency() {
    Optional<SystemConfigurationEntity> config = configurationRepo.findByKey(DEFAULT_CURRENCY_KEY);
    String code = config.map(SystemConfigurationEntity::getValue).orElse(FALLBACK_CURRENCY_CODE);
    try {
      cachedCurrency = Currency.getInstance(code);
    } catch (IllegalArgumentException e) {
      log.warn(
          "Invalid currency code '{}' in configuration, falling back to {}",
          code,
          FALLBACK_CURRENCY_CODE);
      cachedCurrency = Currency.getInstance(FALLBACK_CURRENCY_CODE);
    }
    return cachedCurrency;
  }
}
