/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.AlertSeverity;
import aros.services.rms.core.analytics.domain.AlertStatus;
import aros.services.rms.core.analytics.domain.AlertsPage;
import aros.services.rms.core.analytics.domain.AlertsPage.PageInfo;
import aros.services.rms.core.analytics.domain.AnalyticsPeriods;
import aros.services.rms.core.analytics.domain.SystemAlert;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsPeriodException;
import aros.services.rms.core.analytics.domain.port.in.GetAlertsUseCase;
import aros.services.rms.core.analytics.domain.port.out.SystemAlertRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Reads paged alerts with optional status, severity and period filters. */
@Service
@RequiredArgsConstructor
public class GetAlertsService implements GetAlertsUseCase {

  private static final int MAX_LIMIT = 100;
  private static final int DEFAULT_LIMIT = 50;

  private final SystemAlertRepositoryPort alertRepositoryPort;

  /** {@inheritDoc} */
  @Override
  public AlertsPage execute(
      AlertStatus status,
      AlertSeverity severity,
      String bucket,
      String from,
      String to,
      int limit,
      int offset) {
    int effectiveLimit = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
    int effectiveOffset = Math.max(offset, 0);

    String periodBucket = bucket;
    if (bucket != null && !bucket.isBlank()) {
      AnalyticsPeriods.validateBucket(bucket);
    } else {
      periodBucket = null;
    }
    validatePeriodRange(from, to);

    List<SystemAlert> items =
        alertRepositoryPort.find(
            status, severity, periodBucket, from, to, effectiveLimit, effectiveOffset);
    long total = alertRepositoryPort.count(status, severity, periodBucket, from, to);
    return new AlertsPage(items, new PageInfo(effectiveLimit, effectiveOffset, total));
  }

  private void validatePeriodRange(String from, String to) {
    if ((from == null) != (to == null)) {
      throw new InvalidAnalyticsPeriodException(
          "Se deben indicar 'from' y 'to' juntos al filtrar alertas por periodo.");
    }
    if (from != null && from.compareTo(to) > 0) {
      throw new InvalidAnalyticsPeriodException("'from' debe ser anterior o igual a 'to'.");
    }
  }
}
