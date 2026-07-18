/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.port.in.GetAnalyticsConfigUseCase;
import aros.services.rms.core.analytics.domain.port.out.AnalyticsConfigRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link GetAnalyticsConfigUseCase} that delegates lookup of the singleton
 * analytics configuration to the configured repository port.
 */
@Service
@RequiredArgsConstructor
public class GetAnalyticsConfigService implements GetAnalyticsConfigUseCase {

  private final AnalyticsConfigRepositoryPort repo;

  /**
   * Returns the singleton analytics configuration as held by the repository port.
   *
   * @return the singleton analytics configuration
   */
  @Override
  public AnalyticsConfig get() {
    return repo.findSingleton();
  }
}
