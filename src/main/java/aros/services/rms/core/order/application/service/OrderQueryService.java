/* (C) 2026 */

package aros.services.rms.core.order.application.service;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.OrderQueryUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementación del caso de uso para consultar órdenes. Soporta filtros por estado y rango de
 * fechas.
 */
public class OrderQueryService implements OrderQueryUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(OrderQueryService.class);
  private final OrderRepositoryPort orderRepositoryPort;

  /**
   * Creates a new order query service instance.
   *
   * @param orderRepositoryPort the order repository port
   */
  public OrderQueryService(OrderRepositoryPort orderRepositoryPort) {
    this.orderRepositoryPort = orderRepositoryPort;
  }

  /** {@inheritDoc} Valida que endDate no sea futura y startDate <= endDate. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<Order> findOrders(
      OrderStatus status, LocalDateTime startDate, LocalDateTime endDate) {
    boolean hasStatus = status != null;
    boolean hasDates = startDate != null && endDate != null;

    if (hasDates && endDate.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("End date cannot be in the future");
    }

    if (hasDates && startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }

    if (hasStatus && hasDates) {
      return orderRepositoryPort.findByStatusAndDateBetween(status, startDate, endDate);
    }

    if (hasStatus) {
      return orderRepositoryPort.findByStatus(status);
    }

    if (hasDates) {
      return orderRepositoryPort.findByDateBetween(startDate, endDate);
    }

    return orderRepositoryPort.findAll();
  }

  /**
   * Recovery handler for findOrders operation when database is unavailable.
   *
   * @param e the data access exception
   * @param status the order status filter
   * @param startDate the start date filter
   * @param endDate the end date filter
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public List<Order> recoverFindOrders(
      DataAccessException e, OrderStatus status, LocalDateTime startDate, LocalDateTime endDate) {
    log.warn("BD no disponible - fallback para findOrders: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
