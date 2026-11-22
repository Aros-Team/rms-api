/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.TopSellingProduct;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsRangeException;
import aros.services.rms.core.analytics.domain.port.in.GetTopSellingProductsUseCase;
import aros.services.rms.core.analytics.domain.port.out.TopSellingQueryPort;
import aros.services.rms.core.analytics.domain.port.out.TopSellingQueryPort.ProductSales;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Ranks products by units sold for the dashboard top-products widget. */
@Service
@RequiredArgsConstructor
public class GetTopSellingProductsService implements GetTopSellingProductsUseCase {

  private static final int MAX_LIMIT = 100;

  private final TopSellingQueryPort topSellingQueryPort;

  /** {@inheritDoc} */
  @Override
  public List<TopSellingProduct> execute(LocalDate from, LocalDate to, int limit) {
    if (from == null || to == null) {
      throw new IllegalArgumentException("Both 'from' and 'to' are required.");
    }
    if (to.isBefore(from)) {
      throw new InvalidAnalyticsRangeException("'to' must not be before 'from'.");
    }
    int effectiveLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
    List<TopSellingProduct> products = new ArrayList<>();
    int rank = 1;
    for (ProductSales sales :
        topSellingQueryPort.findTopSelling(from, to.plusDays(1), effectiveLimit)) {
      products.add(
          new TopSellingProduct(
              rank++, sales.productId(), sales.productName(), sales.unitsSold(), sales.revenue()));
    }
    return products;
  }
}
