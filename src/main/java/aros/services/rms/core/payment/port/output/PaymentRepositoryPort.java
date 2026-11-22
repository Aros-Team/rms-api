/* (C) 2026 */

package aros.services.rms.core.payment.port.output;

import aros.services.rms.core.payment.application.dto.PaymentFilter;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentQueryResult;
import java.util.List;
import java.util.Optional;

/** Output port for payment persistence. */
public interface PaymentRepositoryPort {

  /**
   * Inserts or updates a payment.
   *
   * @param payment the payment to save
   * @return the saved payment, including the generated receipt number
   */
  Payment save(Payment payment);

  /**
   * Finds a payment by id, including its tenders.
   *
   * @param id the payment id
   * @return the payment when present
   */
  Optional<Payment> findById(Long id);

  /**
   * Finds a payment by its idempotency key.
   *
   * @param idempotencyKey the client-generated key
   * @return the original payment when present
   */
  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  /**
   * Finds the non-voided payment of an order.
   *
   * @param orderId the order id
   * @return the active payment when present
   */
  Optional<Payment> findActiveByOrderId(Long orderId);

  /**
   * Returns every payment of a shift, voided included, with tenders.
   *
   * @param shiftId the shift id
   * @return the shift payments
   */
  List<Payment> findByShiftId(Long shiftId);

  /**
   * Returns a paged payment journal.
   *
   * @param filter the filters and pagination
   * @return one page of payments
   */
  PaymentQueryResult search(PaymentFilter filter);
}
