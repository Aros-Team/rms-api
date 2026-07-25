/* (C) 2026 */

package aros.services.rms.core.order.domain;

import java.util.List;

/**
 * Paginated query result for orders. Carries the items for the current page plus pagination
 * metadata.
 */
public record OrderQueryResult(List<Order> items, long total, int page, int size) {
  /**
   * Calculates the total number of pages.
   *
   * @return total pages
   */
  public int totalPages() {
    return size > 0 ? (int) Math.ceil((double) total / size) : 0;
  }

  /**
   * Whether there is a next page available.
   *
   * @return true if there are more pages
   */
  public boolean hasNext() {
    return page < totalPages() - 1;
  }

  /**
   * Whether there is a previous page available.
   *
   * @return true if there are previous pages
   */
  public boolean hasPrevious() {
    return page > 0;
  }
}
