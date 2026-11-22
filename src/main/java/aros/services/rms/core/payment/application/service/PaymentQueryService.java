/* (C) 2026 */

package aros.services.rms.core.payment.application.service;

import aros.services.rms.core.payment.application.dto.PaymentFilter;
import aros.services.rms.core.payment.application.exception.PaymentNotFoundException;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentQueryResult;
import aros.services.rms.core.payment.domain.PendingOrderPage;
import aros.services.rms.core.payment.port.input.PaymentQueryUseCase;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import aros.services.rms.core.payment.port.output.PendingOrderQueryPort;
import java.time.LocalDate;

/** Read model for pending orders and the payment journal. */
public class PaymentQueryService implements PaymentQueryUseCase {

  private final PaymentRepositoryPort paymentRepositoryPort;
  private final PendingOrderQueryPort pendingOrderQueryPort;

  /**
   * Creates the service.
   *
   * @param paymentRepositoryPort payment persistence port
   * @param pendingOrderQueryPort pending-orders read model
   */
  public PaymentQueryService(
      PaymentRepositoryPort paymentRepositoryPort, PendingOrderQueryPort pendingOrderQueryPort) {
    this.paymentRepositoryPort = paymentRepositoryPort;
    this.pendingOrderQueryPort = pendingOrderQueryPort;
  }

  /** {@inheritDoc} */
  @Override
  public PendingOrderPage findPending(LocalDate date, String search, int page, int size) {
    LocalDate businessDate = date != null ? date : LocalDate.now();
    Long searchId = parseSearch(search);
    return pendingOrderQueryPort.findPending(businessDate, searchId, page, size);
  }

  /** {@inheritDoc} */
  @Override
  public Payment findById(Long id) {
    return paymentRepositoryPort.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
  }

  /** {@inheritDoc} */
  @Override
  public PaymentQueryResult search(PaymentFilter filter) {
    return paymentRepositoryPort.search(filter);
  }

  /**
   * The cashier search is an exact order id or table number. Non-numeric input cannot match
   * anything, so it is mapped to a sentinel that yields an empty page.
   */
  private static Long parseSearch(String search) {
    if (search == null || search.isBlank()) {
      return null;
    }
    try {
      return Long.valueOf(search.trim());
    } catch (NumberFormatException e) {
      return -1L;
    }
  }
}
