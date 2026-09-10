/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.PayrollCalculationResult;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.core.user.domain.Salary;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link CalculatePayrollEventService}. */
@ExtendWith(MockitoExtension.class)
class CalculatePayrollEventServiceTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Long USER_ID = 1L;
  private static final BigDecimal HOURLY_RATE = new BigDecimal("15625");

  @Mock private UserRepositoryPort userRepositoryPort;
  @Mock private CurrencyProvider currencyProvider;

  private CalculatePayrollEventService service;

  @BeforeEach
  void setUp() {
    service = new CalculatePayrollEventService(userRepositoryPort, currencyProvider);
    lenient().when(currencyProvider.getCurrency()).thenReturn(COP);
  }

  private User createUser(BigDecimal salaryAmount, Integer expectedHours) {
    User user =
        new User(
            UserId.of(USER_ID),
            "12345678",
            "Test User",
            new UserEmail("test@example.com"),
            "encoded-password",
            "123 Main St",
            "555-0100",
            UserRole.WORKER,
            UserStatus.ACTIVE,
            List.of());
    if (salaryAmount != null) {
      user.setSalary(Salary.of(new Money(salaryAmount, COP)));
    }
    user.setExpectedHoursPerMonth(expectedHours);
    return user;
  }

  // ---------------------------------------------------------------------------
  // UC-01: overtime_returnsHourlyRateTimes1point5
  // ---------------------------------------------------------------------------

  @Test
  void overtime_returnsHourlyRateTimes1point5() {
    // hourly_rate = 3000000 / 192 = 15625
    User user = createUser(new BigDecimal("3000000"), 192);
    when(userRepositoryPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));

    PayrollCalculationResult result = service.calculate(USER_ID, "OVERTIME", new BigDecimal("2.5"));

    assertNotNull(result);
    assertEquals(0, HOURLY_RATE.compareTo(result.hourlyRate()));
    assertEquals(0, new BigDecimal("1.5").compareTo(result.multiplier()));
    assertEquals(0, new BigDecimal("23437.50").compareTo(result.suggestedUnitRate()));
    assertEquals(0, new BigDecimal("58593.75").compareTo(result.suggestedAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-02: nightSurcharge_returnsHourlyRateTimes1point75
  // ---------------------------------------------------------------------------

  @Test
  void nightSurcharge_returnsHourlyRateTimes1point75() {
    User user = createUser(new BigDecimal("3000000"), 192);
    when(userRepositoryPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));

    PayrollCalculationResult result =
        service.calculate(USER_ID, "NIGHT_SURCHARGE", new BigDecimal("1"));

    assertNotNull(result);
    assertEquals(0, HOURLY_RATE.compareTo(result.hourlyRate()));
    assertEquals(0, new BigDecimal("1.75").compareTo(result.multiplier()));
    assertEquals(0, new BigDecimal("27343.75").compareTo(result.suggestedUnitRate()));
    assertEquals(0, new BigDecimal("27343.75").compareTo(result.suggestedAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-03: absence_returnsNegativeHourlyRate
  // ---------------------------------------------------------------------------

  @Test
  void absence_returnsNegativeHourlyRate() {
    User user = createUser(new BigDecimal("3000000"), 192);
    when(userRepositoryPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));

    PayrollCalculationResult result = service.calculate(USER_ID, "ABSENCE", new BigDecimal("8"));

    assertNotNull(result);
    assertEquals(0, HOURLY_RATE.compareTo(result.hourlyRate()));
    assertEquals(0, new BigDecimal("-1").compareTo(result.multiplier()));
    assertEquals(0, new BigDecimal("-15625").compareTo(result.suggestedUnitRate()));
    assertEquals(0, new BigDecimal("-125000").compareTo(result.suggestedAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-04: bonusAttendance_returnsHalfHourlyRate
  // ---------------------------------------------------------------------------

  @Test
  void bonusAttendance_returnsHalfHourlyRate() {
    User user = createUser(new BigDecimal("3000000"), 192);
    when(userRepositoryPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));

    PayrollCalculationResult result =
        service.calculate(USER_ID, "BONUS_ATTENDANCE", new BigDecimal("1"));

    assertNotNull(result);
    assertEquals(0, HOURLY_RATE.compareTo(result.hourlyRate()));
    assertEquals(0, new BigDecimal("0.5").compareTo(result.multiplier()));
    assertEquals(0, new BigDecimal("7812.50").compareTo(result.suggestedUnitRate()));
    assertEquals(0, new BigDecimal("7812.50").compareTo(result.suggestedAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-05: bonusPerformance_returnsNullMultiplier
  // ---------------------------------------------------------------------------

  @Test
  void bonusPerformance_returnsNullMultiplier() {
    User user = createUser(new BigDecimal("3000000"), 192);
    when(userRepositoryPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));

    PayrollCalculationResult result =
        service.calculate(USER_ID, "BONUS_PERFORMANCE", new BigDecimal("1"));

    assertNotNull(result);
    assertEquals(0, HOURLY_RATE.compareTo(result.hourlyRate()));
    assertNull(result.multiplier());
    assertNull(result.suggestedUnitRate());
    assertNull(result.suggestedAmount());
  }

  // ---------------------------------------------------------------------------
  // UC-06: deduction_returnsNullMultiplier
  // ---------------------------------------------------------------------------

  @Test
  void deduction_returnsNullMultiplier() {
    User user = createUser(new BigDecimal("3000000"), 192);
    when(userRepositoryPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));

    PayrollCalculationResult result = service.calculate(USER_ID, "DEDUCTION", null);

    assertNotNull(result);
    assertEquals(0, HOURLY_RATE.compareTo(result.hourlyRate()));
    assertNull(result.multiplier());
    assertNull(result.suggestedUnitRate());
    assertNull(result.suggestedAmount());
  }

  // ---------------------------------------------------------------------------
  // UC-07: userNotFound_returnsNull
  // ---------------------------------------------------------------------------

  @Test
  void userNotFound_returnsNull() {
    when(userRepositoryPort.findById(UserId.of(USER_ID))).thenReturn(Optional.empty());

    PayrollCalculationResult result = service.calculate(USER_ID, "OVERTIME", new BigDecimal("2.5"));

    assertNull(result);
  }

  // ---------------------------------------------------------------------------
  // UC-08: overtime_nullQuantity_noSuggestedAmount
  // ---------------------------------------------------------------------------

  @Test
  void overtime_nullQuantity_noSuggestedAmount() {
    User user = createUser(new BigDecimal("3000000"), 192);
    when(userRepositoryPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));

    PayrollCalculationResult result = service.calculate(USER_ID, "OVERTIME", null);

    assertNotNull(result);
    assertEquals(0, HOURLY_RATE.compareTo(result.hourlyRate()));
    assertEquals(0, new BigDecimal("1.5").compareTo(result.multiplier()));
    assertEquals(0, new BigDecimal("23437.50").compareTo(result.suggestedUnitRate()));
    assertNull(result.suggestedAmount());
  }
}
