/* (C) 2026 */

package aros.services.rms.infraestructure.payment.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.application.dto.PaymentFilter;
import aros.services.rms.core.payment.domain.DiscountType;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentDiscount;
import aros.services.rms.core.payment.domain.PaymentQueryResult;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.domain.PaymentTender;
import aros.services.rms.core.payment.domain.PaymentTenderMethod;
import aros.services.rms.core.payment.domain.PendingOrder;
import aros.services.rms.core.payment.domain.PendingOrderPage;
import aros.services.rms.core.payment.domain.PendingPaymentStatus;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.infraestructure.order.persistence.Order;
import aros.services.rms.infraestructure.order.persistence.OrderStatus;
import aros.services.rms.infraestructure.order.persistence.jpa.OrderRepository;
import aros.services.rms.infraestructure.table.persistence.Table;
import aros.services.rms.infraestructure.table.persistence.jpa.TableRepository;
import aros.services.rms.testsupport.AbstractJwtIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link PaymentPersistenceAdapter}: the pending-orders read model and payment journal
 * query.
 *
 * <p><b>Feature:</b> The cashier board must list delivered orders without an active payment.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>An order with no payment is pending
 *   <li>An order with a completed payment is not pending
 *   <li>An order whose only payment is voided is pending again and labelled VOIDED
 *   <li>Searching by table number and filtering the journal by method work
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Boots the context on H2, inserts orders and payments, and runs the real queries
 *   <li>Each test rolls back
 * </ul>
 */
@SpringBootTest(
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.sql.init.mode=never",
      "spring.datasource.url=jdbc:h2:mem:paymentpersistdb;DB_CLOSE_DELAY=-1;MODE=MYSQL",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "app.admin.email=admin@test.local",
      "app.admin.dummy-email=test@test.local",
      "app.admin.password=TestPassword123!",
      "app.env=development"
    })
@Transactional
class PaymentPersistenceAdapterTest extends AbstractJwtIntegrationTest {

  @Autowired private PendingOrderPersistenceAdapter pendingAdapter;
  @Autowired private PaymentPersistenceAdapter paymentAdapter;
  @Autowired private OrderRepository orderRepository;
  @Autowired private TableRepository tableRepository;
  @Autowired private PaymentJpaRepository paymentJpaRepository;
  @MockitoBean private CurrencyProvider currencyProvider;

  private static final LocalDate TODAY = LocalDate.of(2026, 11, 21);

  @BeforeEach
  void setUp() {
    when(currencyProvider.getCurrency()).thenReturn(Currency.getInstance("COP"));
    paymentJpaRepository.deleteAll();
    orderRepository.deleteAll();
    tableRepository.deleteAll();
  }

  @Test
  void should_list_unpaid_and_previously_voided_orders_only() {
    Table table = tableRepository.save(Table.builder().tableNumber(3).capacity(4).build());
    final Order unpaid = orderRepository.save(order(table, TODAY.atTime(19, 58)));
    Order paid = orderRepository.save(order(table, TODAY.atTime(20, 10)));
    Order voided = orderRepository.save(order(table, TODAY.atTime(20, 20)));

    paymentJpaRepository.save(payment(paid.getId(), PaymentStatus.COMPLETED, "key-paid"));
    paymentJpaRepository.save(payment(voided.getId(), PaymentStatus.VOIDED, "key-voided"));

    PendingOrderPage page = pendingAdapter.findPending(TODAY, null, 0, 20);

    assertEquals(2, page.total());
    List<Long> ids = page.items().stream().map(PendingOrder::orderId).collect(Collectors.toList());
    assertTrue(ids.contains(unpaid.getId()));
    assertTrue(ids.contains(voided.getId()));
    assertEquals(
        PendingPaymentStatus.UNPAID,
        page.items().stream()
            .filter(item -> item.orderId().equals(unpaid.getId()))
            .findFirst()
            .orElseThrow()
            .paymentStatus());
    assertEquals(
        PendingPaymentStatus.VOIDED,
        page.items().stream()
            .filter(item -> item.orderId().equals(voided.getId()))
            .findFirst()
            .orElseThrow()
            .paymentStatus());
  }

  @Test
  void should_filter_pending_orders_by_table_number() {
    Table table3 = tableRepository.save(Table.builder().tableNumber(3).capacity(4).build());
    Table table4 = tableRepository.save(Table.builder().tableNumber(4).capacity(4).build());
    Order first = orderRepository.save(order(table3, TODAY.atTime(19, 58)));
    orderRepository.save(order(table4, TODAY.atTime(20, 10)));

    PendingOrderPage page = pendingAdapter.findPending(TODAY, 3L, 0, 20);

    assertEquals(1, page.total());
    assertEquals(first.getId(), page.items().get(0).orderId());
  }

  @Test
  void should_persist_the_payment_tenders_and_assign_the_receipt_number() {
    Payment domain =
        Payment.builder()
            .orderId(10L)
            .shiftId(12L)
            .status(PaymentStatus.COMPLETED)
            .subtotal(money("58000.00"))
            .discount(
                new PaymentDiscount(DiscountType.PERCENT, 1000, money("5800.00"), "Test discount"))
            .tip(money("0.00"))
            .total(money("52200.00"))
            .tendered(money("52200.00"))
            .change(money("0.00"))
            .tenders(
                List.of(
                    new PaymentTender(null, PaymentTenderMethod.CASH, money("52200.00"), null, 0)))
            .cashierId(1001L)
            .idempotencyKey("adapter-key")
            .createdAt(TODAY.atTime(20, 0))
            .completedAt(TODAY.atTime(20, 0))
            .build();

    Payment saved = paymentAdapter.save(domain);

    assertNotNull(saved.getId());
    assertTrue(saved.getReceiptNumber().startsWith("R-20261121-"));
    Payment loaded = paymentAdapter.findById(saved.getId()).orElseThrow();
    assertEquals(new BigDecimal("52200.00"), loaded.getTotal().amount());
    assertEquals(1000, loaded.getDiscount().percentageBasisPoints());
    assertEquals(new BigDecimal("5800.00"), loaded.getDiscount().amount().amount());
    assertEquals(1, loaded.getTenders().size());
    assertEquals(PaymentTenderMethod.CASH, loaded.getTenders().get(0).method());
  }

  @Test
  void should_filter_the_payment_journal_by_method_and_status() {
    Table table = tableRepository.save(Table.builder().tableNumber(3).capacity(4).build());
    Order cashOrder = orderRepository.save(order(table, TODAY.atTime(19, 58)));
    Order cardOrder = orderRepository.save(order(table, TODAY.atTime(20, 10)));

    paymentJpaRepository.save(
        paymentWithTender(
            cashOrder.getId(), PaymentTenderMethod.CASH, PaymentStatus.COMPLETED, "key-cash"));
    paymentJpaRepository.save(
        paymentWithTender(
            cardOrder.getId(), PaymentTenderMethod.CARD, PaymentStatus.COMPLETED, "key-card"));

    PaymentQueryResult result =
        paymentAdapter.search(
            new PaymentFilter(TODAY, PaymentTenderMethod.CASH, PaymentStatus.COMPLETED, 0, 20));

    assertEquals(1, result.total());
    assertEquals(cashOrder.getId(), result.items().get(0).getOrderId());
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), Currency.getInstance("COP"));
  }

  private static Order order(Table table, LocalDateTime date) {
    return Order.builder()
        .date(date)
        .status(OrderStatus.DELIVERED)
        .table(table)
        .partySize(4)
        .build();
  }

  private static PaymentEntity payment(Long orderId, PaymentStatus status, String key) {
    return paymentEntity(orderId, status, key, List.of());
  }

  private static PaymentEntity paymentWithTender(
      Long orderId, PaymentTenderMethod method, PaymentStatus status, String key) {
    PaymentEntity entity = paymentEntity(orderId, status, key, List.of());
    PaymentTenderEntity tender =
        PaymentTenderEntity.builder()
            .payment(entity)
            .method(method)
            .amount(new BigDecimal("58000.00"))
            .sortOrder(0)
            .build();
    entity.setTenders(List.of(tender));
    return entity;
  }

  private static PaymentEntity paymentEntity(
      Long orderId, PaymentStatus status, String key, List<PaymentTenderEntity> tenders) {
    return PaymentEntity.builder()
        .orderId(orderId)
        .shiftId(12L)
        .status(status)
        .currency("COP")
        .subtotal(new BigDecimal("58000.00"))
        .discountAmount(new BigDecimal("0.00"))
        .tipAmount(new BigDecimal("0.00"))
        .totalAmount(new BigDecimal("58000.00"))
        .tenderedAmount(new BigDecimal("58000.00"))
        .changeAmount(new BigDecimal("0.00"))
        .receiptNumber("R-" + key)
        .cashierId(1001L)
        .idempotencyKey(key)
        .createdAt(TODAY.atTime(20, 0))
        .completedAt(TODAY.atTime(20, 0))
        .tenders(tenders)
        .build();
  }
}
