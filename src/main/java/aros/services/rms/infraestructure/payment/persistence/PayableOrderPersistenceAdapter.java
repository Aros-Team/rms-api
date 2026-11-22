/* (C) 2026 */

package aros.services.rms.infraestructure.payment.persistence;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.payment.domain.PayableOrder;
import aros.services.rms.core.payment.port.output.PayableOrderPort;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.infraestructure.order.persistence.Order;
import aros.services.rms.infraestructure.order.persistence.OrderDetail;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adapter that takes the pessimistic order lock and computes the server-side subtotal. */
@Repository
@RequiredArgsConstructor
@Transactional
public class PayableOrderPersistenceAdapter implements PayableOrderPort {

  private final PayableOrderJpaRepository orderRepository;
  private final CurrencyProvider currencyProvider;

  /** {@inheritDoc} */
  @Override
  public Optional<PayableOrder> findForUpdate(Long orderId) {
    return orderRepository
        .findByIdForUpdate(orderId)
        .map(
            order ->
                new PayableOrder(
                    order.getId(),
                    OrderStatus.valueOf(order.getStatus().name()),
                    subtotal(order),
                    order.getTable() != null ? order.getTable().getId() : null,
                    order.getTable() != null ? order.getTable().getTableNumber() : null,
                    order.getPartySize(),
                    order.getDate()));
  }

  private Money subtotal(Order order) {
    BigDecimal total =
        order.getDetails() == null
            ? BigDecimal.ZERO
            : order.getDetails().stream()
                .map(OrderDetail::getUnitPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new Money(total, currencyProvider.getCurrency());
  }
}
