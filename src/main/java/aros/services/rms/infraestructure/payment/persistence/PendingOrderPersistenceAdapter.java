/* (C) 2026 */

package aros.services.rms.infraestructure.payment.persistence;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.domain.PendingOrder;
import aros.services.rms.core.payment.domain.PendingOrderPage;
import aros.services.rms.core.payment.domain.PendingPaymentStatus;
import aros.services.rms.core.payment.port.output.PendingOrderQueryPort;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.infraestructure.order.persistence.Order;
import aros.services.rms.infraestructure.order.persistence.OrderDetail;
import aros.services.rms.infraestructure.order.persistence.OrderStatus;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** Adapter for the pending-orders read model. */
@Repository
@RequiredArgsConstructor
@Transactional
public class PendingOrderPersistenceAdapter implements PendingOrderQueryPort {

  private static final int MAX_PAGE_SIZE = 100;

  private final PendingOrderJpaRepository orderRepository;
  private final PaymentJpaRepository paymentRepository;
  private final CurrencyProvider currencyProvider;

  /** {@inheritDoc} */
  @Override
  public PendingOrderPage findPending(LocalDate date, Long search, int page, int size) {
    int safePage = Math.max(page, 0);
    int safeSize = size <= 0 ? 20 : Math.min(size, MAX_PAGE_SIZE);
    Integer searchNumber = search != null ? search.intValue() : null;

    Page<Order> result =
        orderRepository.findPending(
            OrderStatus.DELIVERED,
            date.atStartOfDay(),
            date.plusDays(1).atStartOfDay(),
            PaymentStatus.VOIDED,
            search,
            searchNumber,
            PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "date")));

    List<Long> orderIds = result.getContent().stream().map(Order::getId).toList();
    Set<Long> voided =
        orderIds.isEmpty()
            ? Set.of()
            : new HashSet<>(paymentRepository.findOrderIdsByStatus(orderIds, PaymentStatus.VOIDED));

    List<PendingOrder> items =
        result.getContent().stream()
            .map(order -> toPending(order, voided.contains(order.getId())))
            .toList();
    return new PendingOrderPage(items, result.getTotalElements(), safePage, safeSize);
  }

  private PendingOrder toPending(Order order, boolean previouslyVoided) {
    BigDecimal total =
        order.getDetails() == null
            ? BigDecimal.ZERO
            : order.getDetails().stream()
                .map(OrderDetail::getUnitPrice)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new PendingOrder(
        order.getId(),
        order.getTable() != null ? order.getTable().getId() : null,
        order.getTable() != null ? order.getTable().getTableNumber() : null,
        order.getPartySize(),
        order.getDate(),
        new Money(total, currencyProvider.getCurrency()),
        previouslyVoided ? PendingPaymentStatus.VOIDED : PendingPaymentStatus.UNPAID);
  }
}
