/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.transaction;

import aros.services.rms.core.payroll.domain.port.input.VoidSettlementUseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional decorator for {@link VoidSettlementUseCase}.
 *
 * <p>The settlement deletion and the restored pending amount are written in one transaction, so a
 * failure cannot delete a payment without restoring the payroll balance.
 */
public class TransactionalVoidSettlementService implements VoidSettlementUseCase {

  private final VoidSettlementUseCase delegate;

  /**
   * Creates the decorator.
   *
   * @param delegate the framework-agnostic use case
   */
  public TransactionalVoidSettlementService(VoidSettlementUseCase delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void execute(Long settlementId) {
    delegate.execute(settlementId);
  }
}
