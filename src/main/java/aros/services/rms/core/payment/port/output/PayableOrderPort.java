/* (C) 2026 */

package aros.services.rms.core.payment.port.output;

import aros.services.rms.core.payment.domain.PayableOrder;
import java.util.Optional;

/**
 * Output port for the minimal order snapshot needed to charge.
 *
 * <p>Kept in the payments module so the order aggregate is not modified by this lane.
 */
public interface PayableOrderPort {

  /**
   * Loads an order with a pessimistic write lock.
   *
   * @param orderId the order id
   * @return the locked order snapshot when it exists
   */
  Optional<PayableOrder> findForUpdate(Long orderId);
}
