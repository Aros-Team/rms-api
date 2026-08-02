/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.exception.InvalidPayrollPeriodException;
import aros.services.rms.core.payroll.domain.exception.PayrollAlreadyExistsException;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link RegisterPayrollService}. */
@ExtendWith(MockitoExtension.class)
class RegisterPayrollServiceTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Long USER_ID = 1L;

  @Mock private PayrollRepositoryPort payrollRepositoryPort;
  @Mock private Logger logger;

  private RegisterPayrollService service;

  @BeforeEach
  void setUp() {
    service = new RegisterPayrollService(payrollRepositoryPort, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: execute_calculatesNetAmount
  // ---------------------------------------------------------------------------

  @Test
  void execute_calculatesNetAmount() {
    Money baseSalary = new Money(new BigDecimal("2500000"), COP);
    Money bonuses = new Money(new BigDecimal("200000"), COP);
    Money deductions = new Money(new BigDecimal("150000"), COP);

    RegisterPayrollUseCase.RegisterPayrollCommand cmd =
        new RegisterPayrollUseCase.RegisterPayrollCommand(
            USER_ID,
            2026,
            7,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 31),
            baseSalary,
            bonuses,
            deductions,
            new BigDecimal("192"),
            "Overtime included",
            10L);

    Payroll saved =
        new Payroll(
            1L,
            USER_ID,
            java.time.YearMonth.of(2026, 7),
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 31),
            baseSalary,
            bonuses,
            deductions,
            new Money(new BigDecimal("2550000"), COP),
            new BigDecimal("192"),
            PayrollStatus.PENDING,
            "Overtime included",
            10L,
            java.time.Instant.now(),
            java.time.Instant.now());

    when(payrollRepositoryPort.save(any())).thenReturn(saved);

    Payroll result = service.register(cmd);

    ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
    verify(payrollRepositoryPort).save(captor.capture());
    Payroll captured = captor.getValue();

    // netAmount = 2500000 + 200000 - 150000 = 2550000
    assertEquals(0, new BigDecimal("2550000.00").compareTo(captured.netAmount().amount()));
    assertNotNull(result);
  }

  // ---------------------------------------------------------------------------
  // UC-02: execute_pendingStatus
  // ---------------------------------------------------------------------------

  @Test
  void execute_pendingStatus() {
    RegisterPayrollUseCase.RegisterPayrollCommand cmd =
        new RegisterPayrollUseCase.RegisterPayrollCommand(
            USER_ID,
            2026,
            7,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 31),
            new Money(new BigDecimal("1000000"), COP),
            Money.zero(COP),
            Money.zero(COP),
            new BigDecimal("160"),
            null,
            10L);

    Payroll saved =
        new Payroll(
            1L,
            USER_ID,
            java.time.YearMonth.of(2026, 7),
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 31),
            new Money(new BigDecimal("1000000"), COP),
            Money.zero(COP),
            Money.zero(COP),
            new Money(new BigDecimal("1000000"), COP),
            new BigDecimal("160"),
            PayrollStatus.PENDING,
            null,
            10L,
            java.time.Instant.now(),
            java.time.Instant.now());

    when(payrollRepositoryPort.save(any())).thenReturn(saved);

    Payroll result = service.register(cmd);

    assertEquals(PayrollStatus.PENDING, result.status());
  }

  // ---------------------------------------------------------------------------
  // UC-03: execute_rejectsDuplicatePeriod
  // ---------------------------------------------------------------------------

  @Test
  void execute_rejectsDuplicatePeriod() {
    when(payrollRepositoryPort.existsByUserIdAndPeriod(USER_ID, 2026, 7)).thenReturn(true);

    RegisterPayrollUseCase.RegisterPayrollCommand cmd =
        new RegisterPayrollUseCase.RegisterPayrollCommand(
            USER_ID,
            2026,
            7,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 31),
            new Money(new BigDecimal("1000000"), COP),
            Money.zero(COP),
            Money.zero(COP),
            new BigDecimal("160"),
            null,
            10L);

    PayrollAlreadyExistsException ex =
        assertThrows(PayrollAlreadyExistsException.class, () -> service.register(cmd));

    assertNotNull(ex);
    verify(payrollRepositoryPort, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-04: execute_rejectsNegativeNetAmount
  // ---------------------------------------------------------------------------

  @Test
  void execute_rejectsNegativeNetAmount() {
    RegisterPayrollUseCase.RegisterPayrollCommand cmd =
        new RegisterPayrollUseCase.RegisterPayrollCommand(
            USER_ID,
            2026,
            7,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 31),
            new Money(new BigDecimal("100000"), COP),
            Money.zero(COP),
            new Money(new BigDecimal("200000"), COP),
            new BigDecimal("160"),
            null,
            10L);

    InvalidPayrollPeriodException ex =
        assertThrows(InvalidPayrollPeriodException.class, () -> service.register(cmd));

    assertNotNull(ex);
    verify(payrollRepositoryPort, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-05: execute_zeroHours_works
  // ---------------------------------------------------------------------------

  @Test
  void execute_zeroHours_works() {
    Money baseSalary = new Money(new BigDecimal("1000000"), COP);

    RegisterPayrollUseCase.RegisterPayrollCommand cmd =
        new RegisterPayrollUseCase.RegisterPayrollCommand(
            USER_ID,
            2026,
            7,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 31),
            baseSalary,
            Money.zero(COP),
            Money.zero(COP),
            BigDecimal.ZERO,
            null,
            10L);

    Payroll saved =
        new Payroll(
            1L,
            USER_ID,
            java.time.YearMonth.of(2026, 7),
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 31),
            baseSalary,
            Money.zero(COP),
            Money.zero(COP),
            baseSalary,
            BigDecimal.ZERO,
            PayrollStatus.PENDING,
            null,
            10L,
            java.time.Instant.now(),
            java.time.Instant.now());

    when(payrollRepositoryPort.save(any())).thenReturn(saved);

    Payroll result = service.register(cmd);

    assertNotNull(result);
    assertEquals(0, BigDecimal.ZERO.compareTo(result.hoursWorked()));
    assertEquals(PayrollStatus.PENDING, result.status());
  }
}
