/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.transaction;

import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.port.input.RegisterSettlementUseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional decorator for {@link RegisterSettlementUseCase}.
 *
 * <p>The settlement insert and the recompute of paid/pending amounts share one transaction, so a
 * failed recompute cannot persist a payment that the payroll does not account for.
 */
public class TransactionalRegisterSettlementService implements RegisterSettlementUseCase {

  private final RegisterSettlementUseCase delegate;

  /**
   * Creates the decorator.
   *
   * @param delegate the framework-agnostic use case
   */
  public TransactionalRegisterSettlementService(RegisterSettlementUseCase delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public PayrollSettlement execute(PayrollSettlement settlement) {
    return delegate.execute(settlement);
  }
}
