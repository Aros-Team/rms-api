/* (C) 2026 */

package aros.services.rms.core.payment.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.cashshift.application.exception.ShiftClosedException;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.application.dto.VoidPaymentCommand;
import aros.services.rms.core.payment.application.exception.InvalidPaymentException;
import aros.services.rms.core.payment.application.exception.PaymentAlreadyVoidedException;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link VoidPaymentService}.
 *
 * <p><b>Feature:</b> Full void of a completed payment while its shift is open.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>A void requires a non-blank reason
 *   <li>An already voided payment is rejected
 *   <li>A payment cannot be voided after its shift is closed
 *   <li>A successful void keeps the row and stamps who/when/why
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the payment and shift ports
 *   <li>Asserts the state transition and the error cases
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class VoidPaymentServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private PaymentRepositoryPort paymentRepositoryPort;
  @Mock private CashShiftRepositoryPort cashShiftRepositoryPort;

  private VoidPaymentService service;

  @BeforeEach
  void setUp() {
    service = new VoidPaymentService(paymentRepositoryPort, cashShiftRepositoryPort);
  }

  @Test
  void shouldVoidPaymentAndKeepAuditFields() {
    Payment payment = payment(PaymentStatus.COMPLETED);
    when(paymentRepositoryPort.findById(9001L)).thenReturn(Optional.of(payment));
    when(cashShiftRepositoryPort.findById(12L))
        .thenReturn(Optional.of(shift(CashShiftStatus.OPEN)));
    when(paymentRepositoryPort.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Payment result =
        service.voidPayment(new VoidPaymentCommand(9001L, "  Cobro duplicado  ", 1002L));

    assertEquals(PaymentStatus.VOIDED, result.getStatus());
    assertEquals("Cobro duplicado", result.getVoidReason());
    assertEquals(1002L, result.getVoidedBy());
    assertNotNull(result.getVoidedAt());
  }

  @Test
  void shouldRejectVoidWithoutReason() {
    assertThrows(
        InvalidPaymentException.class,
        () -> service.voidPayment(new VoidPaymentCommand(9001L, "   ", 1002L)));
  }

  @Test
  void shouldRejectSecondVoid() {
    when(paymentRepositoryPort.findById(9001L))
        .thenReturn(Optional.of(payment(PaymentStatus.VOIDED)));

    assertThrows(
        PaymentAlreadyVoidedException.class,
        () -> service.voidPayment(new VoidPaymentCommand(9001L, "Duplicate", 1002L)));
  }

  @Test
  void shouldRejectVoidAfterShiftClosed() {
    when(paymentRepositoryPort.findById(9001L))
        .thenReturn(Optional.of(payment(PaymentStatus.COMPLETED)));
    when(cashShiftRepositoryPort.findById(12L))
        .thenReturn(Optional.of(shift(CashShiftStatus.CLOSED)));

    assertThrows(
        ShiftClosedException.class,
        () -> service.voidPayment(new VoidPaymentCommand(9001L, "Mistake", 1002L)));
  }

  private static Payment payment(PaymentStatus status) {
    return Payment.builder()
        .id(9001L)
        .orderId(10L)
        .shiftId(12L)
        .status(status)
        .subtotal(money("58000.00"))
        .tip(money("0.00"))
        .total(money("58000.00"))
        .tendered(money("58000.00"))
        .change(money("0.00"))
        .cashierId(1001L)
        .createdAt(LocalDateTime.of(2026, 11, 21, 20, 14))
        .completedAt(LocalDateTime.of(2026, 11, 21, 20, 14))
        .build();
  }

  private static CashShift shift(CashShiftStatus status) {
    return CashShift.builder()
        .id(12L)
        .status(status)
        .openingFloat(money("200000.00"))
        .openedBy(1001L)
        .openedAt(LocalDateTime.of(2026, 11, 21, 11, 2))
        .build();
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
