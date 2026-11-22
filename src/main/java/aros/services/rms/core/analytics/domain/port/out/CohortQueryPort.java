/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.out;

import aros.services.rms.core.common.money.domain.Money;
import java.time.LocalDateTime;
import java.util.List;

/** Output port that loads order visits for the cohort report. */
public interface CohortQueryPort {

  /** One order visit with its resolved fingerprint. */
  record CohortOrderRow(
      Long id,
      LocalDateTime orderDate,
      String fingerprint,
      boolean hasPhone,
      long covers,
      Money total) {}

  /**
   * Loads non-cancelled orders in a half-open datetime range, ordered by date.
   *
   * @param fromInclusive inclusive start instant
   * @param toExclusive exclusive end instant
   * @return the order visits
   */
  List<CohortOrderRow> findOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive);
}
