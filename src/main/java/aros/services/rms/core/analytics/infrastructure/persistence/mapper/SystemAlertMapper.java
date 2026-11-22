/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.mapper;

import aros.services.rms.core.analytics.domain.SystemAlert;
import aros.services.rms.core.analytics.infrastructure.persistence.entity.SystemAlertEntity;
import org.springframework.stereotype.Component;

/** Maps between {@link SystemAlert} and {@link SystemAlertEntity}. */
@Component
public class SystemAlertMapper {

  /**
   * Converts an entity to its domain representation.
   *
   * @param entity the entity
   * @return the domain alert, or null
   */
  public SystemAlert toDomain(SystemAlertEntity entity) {
    if (entity == null) {
      return null;
    }
    return SystemAlert.builder()
        .id(entity.getId())
        .severity(entity.getSeverity())
        .type(entity.getType())
        .metric(entity.getMetric())
        .baseline(entity.getBaseline())
        .current(entity.getCurrent())
        .deviationPct(entity.getDeviationPct())
        .periodKey(entity.getPeriodKey())
        .periodBucket(entity.getPeriodBucket())
        .message(entity.getMessage())
        .detectedAt(entity.getDetectedAt())
        .status(entity.getStatus())
        .readAt(entity.getReadAt())
        .build();
  }

  /**
   * Converts a domain alert to an entity.
   *
   * @param alert the domain alert
   * @return the entity, or null
   */
  public SystemAlertEntity toEntity(SystemAlert alert) {
    if (alert == null) {
      return null;
    }
    return SystemAlertEntity.builder()
        .id(alert.getId())
        .severity(alert.getSeverity())
        .type(alert.getType())
        .metric(alert.getMetric())
        .baseline(alert.getBaseline())
        .current(alert.getCurrent())
        .deviationPct(alert.getDeviationPct())
        .periodKey(alert.getPeriodKey())
        .periodBucket(alert.getPeriodBucket())
        .message(alert.getMessage())
        .detectedAt(alert.getDetectedAt())
        .status(alert.getStatus())
        .readAt(alert.getReadAt())
        .build();
  }
}
