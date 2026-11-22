/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import aros.services.rms.core.analytics.domain.TopSellingProduct;
import java.time.LocalDate;
import java.util.List;

/** Input port for the top-selling products report. */
public interface GetTopSellingProductsUseCase {

  /**
   * Returns products ranked by units sold in the requested date range.
   *
   * @param from inclusive start date
   * @param to inclusive end date
   * @param limit maximum number of products (1..100)
   * @return the ranked products, highest units first
   */
  List<TopSellingProduct> execute(LocalDate from, LocalDate to, int limit);
}
