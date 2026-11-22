/* (C) 2026 */

package aros.services.rms.core.payment.port.output;

import aros.services.rms.core.payment.domain.PendingOrderPage;
import java.time.LocalDate;

/** Output port for the cashier pending-orders read model. */
public interface PendingOrderQueryPort {

  /**
   * Finds delivered orders without an active payment for a business date.
   *
   * @param date the business date
   * @param search exact order id or table number, or {@code null}
   * @param page zero-based page number
   * @param size page size
   * @return one page of pending orders
   */
  PendingOrderPage findPending(LocalDate date, Long search, int page, int size);
}
