/* (C) 2026 */

package aros.services.rms.core.payment.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.cashshift.application.exception.ShiftNotOpenException;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.payment.application.dto.RegisterPaymentCommand;
import aros.services.rms.core.payment.application.exception.InvalidPaymentException;
import aros.services.rms.core.payment.application.exception.OrderAlreadyPaidException;
import aros.services.rms.core.payment.application.exception.OrderNotPayableException;
import aros.services.rms.core.payment.domain.PayableOrder;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentTenderInput;
import aros.services.rms.core.payment.domain.PaymentTenderMethod;
import aros.services.rms.core.payment.domain.RegisteredPayment;
import aros.services.rms.core.payment.port.output.PayableOrderPort;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link RegisterPaymentService}.
 *
 * <p><b>Feature:</b> Charge a delivered order with a server-computed subtotal and an idempotency
 * key.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>The subtotal comes from the order snapshot, never from the client
 *   <li>An existing non-voided payment blocks a second charge
 *   <li>Only DELIVERED orders can be charged and a shift must be open
 *   <li>Replaying the same key returns the original payment
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the order, payment and shift ports
 *   <li>Captures the payment passed to the repository and asserts its money snapshot
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RegisterPaymentServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private PayableOrderPort payableOrderPort;
  @Mock private PaymentRepositoryPort paymentRepositoryPort;
  @Mock private CashShiftRepositoryPort cashShiftRepositoryPort;

  private RegisterPaymentService service;

  @BeforeEach
  void setUp() {
    service =
        new RegisterPaymentService(
            payableOrderPort, paymentRepositoryPort, cashShiftRepositoryPort);
  }

  @Test
  void shouldChargeTheServerOrderSubtotalWithSplitTendersAndChange() {
    when(payableOrderPort.findForUpdate(10L))
        .thenReturn(Optional.of(order(OrderStatus.DELIVERED, "58000.00")));
    when(paymentRepositoryPort.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
    when(paymentRepositoryPort.findActiveByOrderId(10L)).thenReturn(Optional.empty());
    when(cashShiftRepositoryPort.findOpen()).thenReturn(Optional.of(shift(12L)));
    when(paymentRepositoryPort.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RegisteredPayment result =
        service.register(
            command(money("5000.00"), List.of(card("50000.00"), cash("20000.00")), "key-1"));

    Payment payment = result.payment();
    assertEquals(new BigDecimal("58000.00"), payment.getSubtotal().amount());
    assertEquals(new BigDecimal("63000.00"), payment.getTotal().amount());
    assertEquals(new BigDecimal("70000.00"), payment.getTendered().amount());
    assertEquals(new BigDecimal("7000.00"), payment.getChange().amount());
    assertEquals(12L, payment.getShiftId());
    assertEquals(1001L, payment.getCashierId());
    assertEquals(2, payment.getTenders().size());
    assertEquals(PaymentTenderMethod.CASH, payment.getTenders().get(1).method());
    verify(paymentRepositoryPort).save(any(Payment.class));
  }

  @Test
  void shouldReplayTheOriginalPaymentForTheSameIdempotencyKey() {
    Payment original = existingPayment(9001L, 10L);
    when(payableOrderPort.findForUpdate(10L))
        .thenReturn(Optional.of(order(OrderStatus.DELIVERED, "58000.00")));
    when(paymentRepositoryPort.findByIdempotencyKey("key-1")).thenReturn(Optional.of(original));

    RegisteredPayment result = service.register(command(null, List.of(cash("58000.00")), "key-1"));

    assertTrue(result.replayed());
    assertEquals(9001L, result.payment().getId());
    verify(paymentRepositoryPort, never()).save(any(Payment.class));
  }

  @Test
  void shouldRejectSecondChargeWhenActivePaymentExists() {
    when(payableOrderPort.findForUpdate(10L))
        .thenReturn(Optional.of(order(OrderStatus.DELIVERED, "58000.00")));
    when(paymentRepositoryPort.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
    when(paymentRepositoryPort.findActiveByOrderId(10L))
        .thenReturn(Optional.of(existingPayment(9001L, 10L)));

    OrderAlreadyPaidException error =
        assertThrows(
            OrderAlreadyPaidException.class,
            () -> service.register(command(null, List.of(cash("58000.00")), "key-2")));

    assertEquals(9001L, error.getPaymentId());
  }

  @Test
  void shouldRejectAnOrderThatIsNotDelivered() {
    when(payableOrderPort.findForUpdate(10L))
        .thenReturn(Optional.of(order(OrderStatus.PREPARING, "58000.00")));
    when(paymentRepositoryPort.findByIdempotencyKey("key-3")).thenReturn(Optional.empty());

    assertThrows(
        OrderNotPayableException.class,
        () -> service.register(command(null, List.of(cash("58000.00")), "key-3")));
  }

  @Test
  void shouldRejectChargeWithoutOpenShift() {
    when(payableOrderPort.findForUpdate(10L))
        .thenReturn(Optional.of(order(OrderStatus.DELIVERED, "58000.00")));
    when(paymentRepositoryPort.findByIdempotencyKey("key-4")).thenReturn(Optional.empty());
    when(paymentRepositoryPort.findActiveByOrderId(10L)).thenReturn(Optional.empty());
    when(cashShiftRepositoryPort.findOpen()).thenReturn(Optional.empty());

    assertThrows(
        ShiftNotOpenException.class,
        () -> service.register(command(null, List.of(cash("58000.00")), "key-4")));
  }

  @Test
  void shouldRejectAnIdempotencyKeyReusedForAnotherOrder() {
    when(payableOrderPort.findForUpdate(10L))
        .thenReturn(Optional.of(order(OrderStatus.DELIVERED, "58000.00")));
    when(paymentRepositoryPort.findByIdempotencyKey("key-5"))
        .thenReturn(Optional.of(existingPayment(9001L, 99L)));

    assertThrows(
        InvalidPaymentException.class,
        () -> service.register(command(null, List.of(cash("58000.00")), "key-5")));
  }

  @Test
  void shouldStoreTheReceiptGeneratedByTheRepository() {
    when(payableOrderPort.findForUpdate(10L))
        .thenReturn(Optional.of(order(OrderStatus.DELIVERED, "58000.00")));
    when(paymentRepositoryPort.findByIdempotencyKey("key-6")).thenReturn(Optional.empty());
    when(paymentRepositoryPort.findActiveByOrderId(10L)).thenReturn(Optional.empty());
    when(cashShiftRepositoryPort.findOpen()).thenReturn(Optional.of(shift(12L)));
    when(paymentRepositoryPort.save(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              payment.setId(9001L);
              payment.setReceiptNumber("R-20261121-009001");
              return payment;
            });

    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    RegisteredPayment result = service.register(command(null, List.of(cash("58000.00")), "key-6"));
    verify(paymentRepositoryPort).save(captor.capture());

    assertEquals("R-20261121-009001", result.payment().getReceiptNumber());
    assertEquals(new BigDecimal("0.00"), captor.getValue().getTip().amount());
  }

  private static PayableOrder order(OrderStatus status, String subtotal) {
    return new PayableOrder(
        10L, status, money(subtotal), 3L, 3, 4, LocalDateTime.of(2026, 11, 21, 19, 58));
  }

  private static CashShift shift(Long id) {
    return CashShift.builder()
        .id(id)
        .status(CashShiftStatus.OPEN)
        .openingFloat(money("200000.00"))
        .openedBy(1001L)
        .openedAt(LocalDateTime.of(2026, 11, 21, 11, 2))
        .build();
  }

  private static Payment existingPayment(Long id, Long orderId) {
    return Payment.builder()
        .id(id)
        .orderId(orderId)
        .subtotal(money("58000.00"))
        .tip(money("0.00"))
        .total(money("58000.00"))
        .tendered(money("58000.00"))
        .change(money("0.00"))
        .build();
  }

  private static RegisterPaymentCommand command(
      Money tip, List<PaymentTenderInput> tenders, String key) {
    return new RegisterPaymentCommand(10L, tip, null, tenders, key, 1001L);
  }

  private static PaymentTenderInput cash(String amount) {
    return new PaymentTenderInput(PaymentTenderMethod.CASH, money(amount), null);
  }

  private static PaymentTenderInput card(String amount) {
    return new PaymentTenderInput(PaymentTenderMethod.CARD, money(amount), "AUTH-1");
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
