/* (C) 2026 */

package aros.services.rms.core.cashshift.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.cashshift.application.dto.OpenCashShiftCommand;
import aros.services.rms.core.cashshift.application.exception.InvalidCashShiftException;
import aros.services.rms.core.cashshift.application.exception.ShiftAlreadyOpenException;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
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
 * Tests for {@link OpenCashShiftService}.
 *
 * <p><b>Feature:</b> Open a cash shift with a float, only when no other shift is open.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>A non-negative float opens an OPEN shift owned by the current user
 *   <li>A negative float is rejected
 *   <li>Opening while another shift is open is rejected
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the shift repository and currency provider
 *   <li>Asserts the saved shift and the rejected cases
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class OpenCashShiftServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private CashShiftRepositoryPort cashShiftRepositoryPort;

  private OpenCashShiftService service;

  @BeforeEach
  void setUp() {
    service = new OpenCashShiftService(cashShiftRepositoryPort);
  }

  @Test
  void shouldOpenShiftWithFloatAndCurrentUser() {
    when(cashShiftRepositoryPort.findOpen()).thenReturn(Optional.empty());
    when(cashShiftRepositoryPort.save(any(CashShift.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CashShift shift = service.open(new OpenCashShiftCommand(money("200000.00"), 1001L));

    assertEquals(CashShiftStatus.OPEN, shift.getStatus());
    assertEquals(new BigDecimal("200000.00"), shift.getOpeningFloat().amount());
    assertEquals(1001L, shift.getOpenedBy());
  }

  @Test
  void shouldRejectNegativeFloat() {
    assertThrows(
        InvalidCashShiftException.class,
        () -> service.open(new OpenCashShiftCommand(money("-1.00"), 1001L)));
  }

  @Test
  void shouldRejectOpeningSecondShift() {
    when(cashShiftRepositoryPort.findOpen())
        .thenReturn(
            Optional.of(
                CashShift.builder()
                    .id(12L)
                    .status(CashShiftStatus.OPEN)
                    .openingFloat(money("100000.00"))
                    .openedBy(1001L)
                    .openedAt(LocalDateTime.now())
                    .build()));

    assertThrows(
        ShiftAlreadyOpenException.class,
        () -> service.open(new OpenCashShiftCommand(money("100000.00"), 1002L)));
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
