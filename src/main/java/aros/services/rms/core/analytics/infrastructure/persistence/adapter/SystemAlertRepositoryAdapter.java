/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.adapter;

import aros.services.rms.core.analytics.domain.AlertSeverity;
import aros.services.rms.core.analytics.domain.AlertStatus;
import aros.services.rms.core.analytics.domain.SystemAlert;
import aros.services.rms.core.analytics.domain.port.out.SystemAlertRepositoryPort;
import aros.services.rms.core.analytics.infrastructure.persistence.entity.SystemAlertEntity;
import aros.services.rms.core.analytics.infrastructure.persistence.mapper.SystemAlertMapper;
import aros.services.rms.core.analytics.infrastructure.persistence.repository.JpaSystemAlertRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA-backed adapter for system_alerts persistence. */
@Component
@RequiredArgsConstructor
public class SystemAlertRepositoryAdapter implements SystemAlertRepositoryPort {

  private final JpaSystemAlertRepository jpaRepo;
  private final SystemAlertMapper mapper;

  /** {@inheritDoc} */
  @Override
  public List<SystemAlert> find(
      AlertStatus status,
      AlertSeverity severity,
      String bucket,
      String from,
      String to,
      int limit,
      int offset) {
    int fetch = Math.min(offset + limit, 10_000);
    return jpaRepo
        .findFiltered(status, severity, bucket, from, to, PageRequest.of(0, fetch))
        .stream()
        .skip(offset)
        .limit(limit)
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  /** {@inheritDoc} */
  @Override
  public long count(
      AlertStatus status, AlertSeverity severity, String bucket, String from, String to) {
    return jpaRepo.countFiltered(status, severity, bucket, from, to);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<SystemAlert> findById(Long id) {
    return jpaRepo.findById(id).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void markRead(Long id, Instant readAt) {
    jpaRepo.markRead(id, AlertStatus.READ, AlertStatus.OPEN, readAt);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public boolean insertIfAbsent(SystemAlert alert) {
    if (jpaRepo.existsByTypeAndMetricAndPeriodKey(
        alert.getType(), alert.getMetric(), alert.getPeriodKey())) {
      return false;
    }
    SystemAlertEntity entity = mapper.toEntity(alert);
    entity.setId(null);
    jpaRepo.save(entity);
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public long countAll() {
    return jpaRepo.count();
  }
}
