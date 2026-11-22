/* (C) 2026 */

package aros.services.rms.core.cashshift.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.cashshift.application.dto.CloseCashShiftCommand;
import aros.services.rms.core.cashshift.application.exception.InvalidCashShiftException;
import aros.services.rms.core.cashshift.application.exception.ShiftClosedException;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import aros.services.rms.core.cashshift.domain.CashShiftSummary;
import aros.services.rms.core.cashshift.port.output.CashMovementRepositoryPort;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link CloseCashShiftService}.
 *
 * <p><b>Feature:</b> Close a shift, snapshot expected vs counted cash and compute the difference.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Expected cash equals float plus cash tenders minus change plus movements
 *   <li>Difference equals counted minus expected
 *   <li>An already closed shift or a negative count is rejected
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks payment and movement repositories with an empty shift
 *   <li>Asserts the snapshotted values on the returned summary
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class CloseCashShiftServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private CashShiftRepositoryPort cashShiftRepositoryPort;
  @Mock private PaymentRepositoryPort paymentRepositoryPort;
  @Mock private CashMovementRepositoryPort cashMovementRepositoryPort;

  private CloseCashShiftService service;

  @BeforeEach
  void setUp() {
    service =
        new CloseCashShiftService(
            cashShiftRepositoryPort, paymentRepositoryPort, cashMovementRepositoryPort);
  }

  @Test
  void shouldSnapshotExpectedCountedAndDifference() {
    when(cashShiftRepositoryPort.findById(12L))
        .thenReturn(Optional.of(shift(CashShiftStatus.OPEN)));
    when(paymentRepositoryPort.findByShiftId(12L)).thenReturn(List.of());
    when(cashMovementRepositoryPort.findByShiftId(12L)).thenReturn(List.of());
    when(cashShiftRepositoryPort.save(any(CashShift.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CashShiftSummary summary =
        service.close(
            new CloseCashShiftCommand(12L, money("95000.00"), "Cierre turno noche", 1001L));

    assertEquals(CashShiftStatus.CLOSED, summary.shift().getStatus());
    assertEquals(new BigDecimal("100000.00"), summary.shift().getExpectedCash().amount());
    assertEquals(new BigDecimal("95000.00"), summary.shift().getCountedCash().amount());
    assertEquals(new BigDecimal("-5000.00"), summary.shift().getDifference().amount());
    assertEquals("Cierre turno noche", summary.shift().getNotes());
    assertEquals(1001L, summary.shift().getClosedBy());
    assertEquals(new BigDecimal("100000.00"), summary.totals().expectedCash().amount());
  }

  @Test
  void shouldRejectClosingAnAlreadyClosedShift() {
    when(cashShiftRepositoryPort.findById(12L))
        .thenReturn(Optional.of(shift(CashShiftStatus.CLOSED)));

    assertThrows(
        ShiftClosedException.class,
        () -> service.close(new CloseCashShiftCommand(12L, money("0.00"), null, 1001L)));
  }

  @Test
  void shouldRejectNegativeCountedCash() {
    assertThrows(
        InvalidCashShiftException.class,
        () -> service.close(new CloseCashShiftCommand(12L, money("-1.00"), null, 1001L)));
  }

  private static CashShift shift(CashShiftStatus status) {
    return CashShift.builder()
        .id(12L)
        .status(status)
        .openingFloat(money("100000.00"))
        .openedBy(1001L)
        .openedAt(LocalDateTime.of(2026, 11, 21, 11, 2))
        .build();
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
