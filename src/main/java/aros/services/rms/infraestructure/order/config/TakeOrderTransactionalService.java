/* (C) 2026 */
package aros.services.rms.infraestructure.order.config;

import aros.services.rms.core.order.application.dto.TakeOrderCommand;
import aros.services.rms.core.order.application.service.TakeOrderService;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Infrastructure service that wraps TakeOrderService with Spring @Transactional and @Retryable.
 *
 * <p>The core use case is framework-agnostic and has no transaction or retry annotations. This
 * service is the single transactional entry point for order creation operations. It also implements
 * the TakeOrderUseCase port so it can be injected wherever the port is required.
 *
 * <p>Marked @Primary so Spring resolves this bean when multiple TakeOrderUseCase implementations
 * exist in the context.
 *
 * <p>The @Transactional annotation ensures that the entire order creation flow (including inventory
 * availability checks with pessimistic locking) runs within a single read-write transaction.
 */
@Service
@Primary
public class TakeOrderTransactionalService implements TakeOrderUseCase {

  private static final org.slf4j.Logger log =
      LoggerFactory.getLogger(TakeOrderTransactionalService.class);
  private final TakeOrderService delegate;

  public TakeOrderTransactionalService(
      @Qualifier("takeOrderUseCaseImpl") TakeOrderService delegate) {
    this.delegate = delegate;
  }

  /**
   * Creates a new order with automatic retry on database access failures.
   *
   * <p>This method uses @Transactional to ensure the entire operation (table status update,
   * inventory availability check with pessimistic locking, order creation, and inventory deduction)
   * runs within a single transaction.
   *
   * <p>Retries up to 3 times with 1 second delay on DataAccessException.
   *
   * @param command order creation command
   * @return the created order
   */
  @Override
  @Transactional
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order execute(TakeOrderCommand command) {
    return delegate.execute(command);
  }

  /**
   * Fallback method when all retry attempts fail.
   *
   * @param e the exception that caused the failure
   * @param command the original command
   * @return never returns, always throws ServiceUnavailableException
   */
  @Recover
  public Order recoverExecute(DataAccessException e, TakeOrderCommand command) {
    log.warn(
        "BD no disponible - fallback para execute(tableId={}): {}",
        command.getTableId(),
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
