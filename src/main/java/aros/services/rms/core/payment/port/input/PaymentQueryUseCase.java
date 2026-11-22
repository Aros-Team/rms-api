/* (C) 2026 */

package aros.services.rms.core.payment.port.input;

import aros.services.rms.core.payment.application.dto.PaymentFilter;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentQueryResult;
import aros.services.rms.core.payment.domain.PendingOrderPage;
import java.time.LocalDate;

/** Read use cases owned by the payments module. */
public interface PaymentQueryUseCase {

  /**
   * Finds delivered orders without an active payment for a business date.
   *
   * @param date the business date
   * @param search optional exact order id or table number search
   * @param page zero-based page number
   * @param size page size
   * @return one page of pending orders
   */
  PendingOrderPage findPending(LocalDate date, String search, int page, int size);

  /**
   * Returns a payment by id.
   *
   * @param id the payment id
   * @return the payment
   */
  Payment findById(Long id);

  /**
   * Returns a paged payment journal.
   *
   * @param filter the filters and pagination
   * @return one page of payments
   */
  PaymentQueryResult search(PaymentFilter filter);
}
