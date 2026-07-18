/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.adapter;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.exception.AnalyticsConfigNotFoundException;
import aros.services.rms.core.analytics.domain.port.out.AnalyticsConfigRepositoryPort;
import aros.services.rms.core.analytics.infrastructure.persistence.mapper.AnalyticsConfigMapper;
import aros.services.rms.core.analytics.infrastructure.persistence.repository.JpaAnalyticsConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** JPA-backed adapter for analytics configuration persistence. */
@Component
@RequiredArgsConstructor
public class AnalyticsConfigRepositoryAdapter implements AnalyticsConfigRepositoryPort {

  private final JpaAnalyticsConfigRepository jpaRepo;
  private final AnalyticsConfigMapper mapper;

  /** {@inheritDoc} */
  @Override
  public AnalyticsConfig findSingleton() {
    return jpaRepo
        .findById(1)
        .map(mapper::toDomain)
        .orElseThrow(AnalyticsConfigNotFoundException::new);
  }

  /** {@inheritDoc} */
  @Override
  public AnalyticsConfig save(AnalyticsConfig config) {
    return mapper.toDomain(jpaRepo.save(mapper.toEntity(config)));
  }
}
