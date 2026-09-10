/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.SettlementType;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link ListSettlementsService}. */
@ExtendWith(MockitoExtension.class)
class ListSettlementsServiceTest {

  @Mock private PayrollSettlementRepositoryPort settlementRepository;
  @Mock private Logger logger;

  private ListSettlementsService service;

  @BeforeEach
  void setUp() {
    service = new ListSettlementsService(settlementRepository, logger);
  }

  // ---------------------------------------------------------------------------
  // UC-01: should_list_settlements_for_period
  // ---------------------------------------------------------------------------

  @Test
  void should_list_settlements_for_period() {
    LocalDate startDate = LocalDate.of(2026, 8, 1);
    LocalDate endDate = LocalDate.of(2026, 8, 31);

    PayrollSettlement s1 =
        new PayrollSettlement(
            1L,
            10L,
            1L,
            SettlementType.MONTHLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 31),
            new BigDecimal("2500000"),
            "August",
            Instant.now(),
            "admin");

    PayrollSettlement s2 =
        new PayrollSettlement(
            2L,
            11L,
            1L,
            SettlementType.BIWEEKLY,
            LocalDate.of(2026, 8, 15),
            LocalDate.of(2026, 8, 31),
            new BigDecimal("1250000"),
            "Mid-month",
            Instant.now(),
            "admin");

    when(settlementRepository.findByUserAndPeriod(1L, startDate, endDate))
        .thenReturn(List.of(s1, s2));

    List<PayrollSettlement> result = service.execute(1L, startDate, endDate);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(1L, result.get(0).id());
    assertEquals(2L, result.get(1).id());
    assertEquals(SettlementType.MONTHLY, result.get(0).settlementType());
    assertEquals(SettlementType.BIWEEKLY, result.get(1).settlementType());
    verify(settlementRepository).findByUserAndPeriod(1L, startDate, endDate);
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_return_empty_list_when_no_settlements
  // ---------------------------------------------------------------------------

  @Test
  void should_return_empty_list_when_no_settlements() {
    LocalDate startDate = LocalDate.of(2026, 9, 1);
    LocalDate endDate = LocalDate.of(2026, 9, 30);

    when(settlementRepository.findByUserAndPeriod(99L, startDate, endDate)).thenReturn(List.of());

    List<PayrollSettlement> result = service.execute(99L, startDate, endDate);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(settlementRepository).findByUserAndPeriod(99L, startDate, endDate);
  }
}
