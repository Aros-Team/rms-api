/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.application.service.PayrollAggregationService;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollEventUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import aros.services.rms.infraestructure.payroll.persistence.jpa.PayrollEventRepository;
import aros.services.rms.testsupport.AbstractJwtIntegrationTest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link TransactionalRegisterPayrollEventService}.
 *
 * <p><b>Feature:</b> Event insert and payroll aggregation must share one transaction.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>When the aggregation fails after the event row is inserted, the row is rolled back
 *   <li>The event table is left exactly as it was before the call
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Boots the real persistence adapters on H2
 *   <li>Mocks the aggregation service so it throws after the real insert
 *   <li>Asserts the inserted row is gone, which only holds if the decorator opened a transaction
 * </ul>
 */
@SpringBootTest(
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.sql.init.mode=never",
      "spring.datasource.url=jdbc:h2:mem:payrolltxdb;DB_CLOSE_DELAY=-1;MODE=MYSQL;"
          + "NON_KEYWORDS=KEY,VALUE",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "app.admin.email=admin@test.local",
      "app.admin.dummy-email=test@test.local",
      "app.admin.password=TestPassword123!",
      "app.env=development"
    })
class PayrollTransactionRollbackTest extends AbstractJwtIntegrationTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Long USER_ID = 1001L;
  private static final YearMonth PERIOD = YearMonth.of(2026, 11);
  private static final LocalDate PERIOD_START = PERIOD.atDay(1);
  private static final LocalDate PERIOD_END = PERIOD.atEndOfMonth();

  @Autowired private RegisterPayrollEventUseCase registerEvent;
  @Autowired private PayrollEventRepositoryPort eventRepository;
  @Autowired private PayrollEventRepository eventJpaRepository;

  @MockitoBean private PayrollRepositoryPort payrollRepository;
  @MockitoBean private PayrollAggregationService aggregationService;

  @BeforeEach
  void setUp() {
    eventJpaRepository.deleteAll();
  }

  @Test
  void should_roll_back_event_when_aggregation_fails_after_insert() {
    Payroll payroll = payroll();
    when(payrollRepository.findByUserIdAndPeriod(USER_ID, PERIOD.getYear(), PERIOD.getMonthValue()))
        .thenReturn(Optional.of(payroll));
    doThrow(new IllegalStateException("aggregation failed"))
        .when(aggregationService)
        .recompute(any());

    PayrollEvent event =
        PayrollEvent.create(
            USER_ID,
            PERIOD_START.plusDays(4),
            PayrollEventType.OVERTIME,
            new BigDecimal("2"),
            money("10000"),
            "overtime",
            "test");

    assertThrows(IllegalStateException.class, () -> registerEvent.execute(event));

    assertTrue(eventRepository.findByUserAndPeriod(USER_ID, PERIOD_START, PERIOD_END).isEmpty());
    assertEquals(0, eventJpaRepository.count());
  }

  @Test
  void should_persist_event_when_aggregation_succeeds() {
    Payroll payroll = payroll();
    when(payrollRepository.findByUserIdAndPeriod(USER_ID, PERIOD.getYear(), PERIOD.getMonthValue()))
        .thenReturn(Optional.of(payroll));
    when(aggregationService.recompute(any())).thenReturn(payroll);

    registerEvent.execute(event());

    assertEquals(1, eventJpaRepository.count());
  }

  private static PayrollEvent event() {
    return PayrollEvent.create(
        USER_ID,
        PERIOD_START.plusDays(4),
        PayrollEventType.OVERTIME,
        new BigDecimal("2"),
        money("10000"),
        "overtime",
        "test");
  }

  private static Payroll payroll() {
    return new Payroll(
        1L,
        USER_ID,
        PERIOD,
        PERIOD_START,
        PERIOD_END,
        money("1500000"),
        money("0"),
        money("0"),
        money("0"),
        money("0"),
        money("1500000"),
        money("0"),
        BigDecimal.ZERO,
        PayrollStatus.PENDING,
        null,
        1L,
        Instant.parse("2026-11-01T00:00:00Z"),
        Instant.parse("2026-11-01T00:00:00Z"));
  }

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
