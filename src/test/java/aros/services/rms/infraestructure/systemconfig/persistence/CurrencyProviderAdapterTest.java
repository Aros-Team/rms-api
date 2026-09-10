/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Currency;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link CurrencyProviderAdapter}. */
@ExtendWith(MockitoExtension.class)
class CurrencyProviderAdapterTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Currency USD = Currency.getInstance("USD");
  private static final String CURRENCY_KEY = "default_currency";

  @Mock private SystemConfigurationJpaRepository configurationRepo;

  private CurrencyProviderAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new CurrencyProviderAdapter(configurationRepo);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_return_cop_when_config_exists
  // ---------------------------------------------------------------------------

  @Test
  void should_return_cop_when_config_exists() {
    SystemConfigurationEntity entity = new SystemConfigurationEntity();
    entity.setKey(CURRENCY_KEY);
    entity.setValue("COP");

    when(configurationRepo.findByKey(CURRENCY_KEY)).thenReturn(Optional.of(entity));

    Currency result = adapter.getCurrency();

    assertEquals(COP, result);
    verify(configurationRepo).findByKey(CURRENCY_KEY);
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_fallback_to_cop_when_config_missing
  // ---------------------------------------------------------------------------

  @Test
  void should_fallback_to_cop_when_config_missing() {
    when(configurationRepo.findByKey(CURRENCY_KEY)).thenReturn(Optional.empty());

    Currency result = adapter.getCurrency();

    assertEquals(COP, result);
    verify(configurationRepo).findByKey(CURRENCY_KEY);
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_fallback_to_cop_when_invalid_currency
  // ---------------------------------------------------------------------------

  @Test
  void should_fallback_to_cop_when_invalid_currency() {
    SystemConfigurationEntity entity = new SystemConfigurationEntity();
    entity.setKey(CURRENCY_KEY);
    entity.setValue("INVALID_CURRENCY_CODE");

    when(configurationRepo.findByKey(CURRENCY_KEY)).thenReturn(Optional.of(entity));

    Currency result = adapter.getCurrency();

    assertEquals(COP, result);
    verify(configurationRepo).findByKey(CURRENCY_KEY);
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_cache_currency_after_first_load
  // ---------------------------------------------------------------------------

  @Test
  void should_cache_currency_after_first_load() {
    SystemConfigurationEntity entity = new SystemConfigurationEntity();
    entity.setKey(CURRENCY_KEY);
    entity.setValue("USD");

    when(configurationRepo.findByKey(CURRENCY_KEY)).thenReturn(Optional.of(entity));

    Currency first = adapter.getCurrency();
    Currency second = adapter.getCurrency();

    assertEquals(USD, first);
    assertEquals(USD, second);
    // Repository should only be called once due to caching
    verify(configurationRepo).findByKey(CURRENCY_KEY);
  }

  // ---------------------------------------------------------------------------
  // UC-05: should_refresh_currency_bypasses_cache
  // ---------------------------------------------------------------------------

  @Test
  void should_refresh_currency_bypasses_cache() {
    SystemConfigurationEntity entity = new SystemConfigurationEntity();
    entity.setKey(CURRENCY_KEY);
    entity.setValue("USD");

    when(configurationRepo.findByKey(CURRENCY_KEY)).thenReturn(Optional.of(entity));

    Currency refreshed = adapter.refreshCurrency();

    assertEquals(USD, refreshed);
    verify(configurationRepo).findByKey(CURRENCY_KEY);
  }

  // ---------------------------------------------------------------------------
  // UC-06: should_refresh_resets_cache
  // ---------------------------------------------------------------------------

  @Test
  void should_refresh_resets_cache() {
    SystemConfigurationEntity usdEntity = new SystemConfigurationEntity();
    usdEntity.setKey(CURRENCY_KEY);
    usdEntity.setValue("USD");

    SystemConfigurationEntity copEntity = new SystemConfigurationEntity();
    copEntity.setKey(CURRENCY_KEY);
    copEntity.setValue("COP");

    when(configurationRepo.findByKey(CURRENCY_KEY))
        .thenReturn(Optional.of(usdEntity))
        .thenReturn(Optional.of(copEntity));

    // Load USD
    Currency first = adapter.getCurrency();
    assertEquals(USD, first);

    // Refresh to COP
    Currency refreshed = adapter.refreshCurrency();
    assertEquals(COP, refreshed);

    // New getCurrency should now return COP (cache was reset)
    Currency cached = adapter.getCurrency();
    assertEquals(COP, cached);

    // 2 calls to repo: one from getCurrency (USD), one from refreshCurrency (COP)
    verify(configurationRepo, org.mockito.Mockito.times(2)).findByKey(CURRENCY_KEY);
  }
}
