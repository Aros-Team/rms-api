/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.out;

import aros.services.rms.core.common.money.domain.Money;
import java.time.LocalDate;
import java.util.List;

/** Output port that ranks products by units sold. */
public interface TopSellingQueryPort {

  /** One product sales aggregate. */
  record ProductSales(Long productId, String productName, long unitsSold, Money revenue) {}

  /**
   * Loads product sales for a half-open date range, highest units first.
   *
   * @param startInclusive inclusive start date
   * @param endExclusive exclusive end date
   * @param limit maximum rows
   * @return the ranked product sales
   */
  List<ProductSales> findTopSelling(LocalDate startInclusive, LocalDate endExclusive, int limit);
}
