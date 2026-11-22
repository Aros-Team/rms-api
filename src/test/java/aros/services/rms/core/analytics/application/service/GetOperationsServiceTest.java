/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.OperationsReport;
import aros.services.rms.core.analytics.domain.port.out.AnalyticsConfigRepositoryPort;
import aros.services.rms.core.analytics.domain.port.out.OperationsQueryPort;
import aros.services.rms.core.analytics.domain.port.out.OperationsQueryPort.DayPartCovers;
import aros.services.rms.core.analytics.domain.port.out.OperationsQueryPort.OperationsSnapshot;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Tests for {@link GetOperationsService}.
 *
 * <p><b>Feature:</b> Operational efficiency: RevPASH, covers, table turns and labor share.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Golden G3: 5 tables × 4 seats over one 720-minute day yields 240 available seat hours,
 *       RevPASH 7083.33 and 5.60 turns
 *   <li>Day-part turns are split into LUNCH and DINNER
 *   <li>No orders yields an {@code EMPTY} report
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks {@link OperationsQueryPort} and the analytics configuration port
 *   <li>Executes the service for a single day and for an empty month
 *   <li>Asserts the exact golden rates and completeness flags
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetOperationsServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private OperationsQueryPort operationsQueryPort;
  @Mock private AnalyticsConfigRepositoryPort configRepositoryPort;
  @Mock private CurrencyProvider currencyProvider;

  private GetOperationsService service() {
    when(currencyProvider.getCurrency()).thenReturn(COP);
    when(configRepositoryPort.findSingleton())
        .thenReturn(AnalyticsConfig.defaults(LocalDateTime.of(2026, 11, 21, 0, 0)));
    return new GetOperationsService(operationsQueryPort, configRepositoryPort, currencyProvider);
  }

  @Test
  void should_match_golden_g3_operations() {
    Money netSales = new Money(new BigDecimal("1700000.00"), COP);
    OperationsSnapshot snapshot =
        new OperationsSnapshot(
            10,
            10,
            28,
            500,
            1390,
            netSales,
            List.of(new DayPartCovers("LUNCH", 12), new DayPartCovers("DINNER", 16)),
            5,
            20);
    when(operationsQueryPort.load(
            any(LocalDate.class), any(LocalDate.class), any(), any(), any(), any()))
        .thenReturn(snapshot);
    when(operationsQueryPort.loadLaborCost(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(new Money(new BigDecimal("340000.00"), COP));

    OperationsReport report = service().execute("daily", "2027-01-15", "2027-01-15");

    assertNotNull(report);
    assertEquals("FULL", report.dataCompleteness());
    assertEquals(28, report.covers());
    assertEquals(10, report.orders());
    assertEquals("1700000.00", report.revPash().totalNetSales().amount().toPlainString());
    assertEquals("240.00", report.revPash().totalAvailableSeatHours().toPlainString());
    assertEquals("7083.33", report.revPash().value().amount().toPlainString());
    assertEquals("5.60", report.tableTurnover().overall().toPlainString());
    assertEquals(2, report.tableTurnover().byDayPart().size());
    assertEquals("2.40", report.tableTurnover().byDayPart().get(0).turns().toPlainString());
    assertEquals(12, report.tableTurnover().byDayPart().get(0).covers());
    assertEquals("3.20", report.tableTurnover().byDayPart().get(1).turns().toPlainString());
    assertEquals(16, report.tableTurnover().byDayPart().get(1).covers());
    assertEquals("50.00", report.avgOccupancyMinutes().toPlainString());
    assertEquals(1390, report.totalOccupiedSeatMinutes());
    assertEquals("20.00", report.labor().sharePct().toPlainString());
  }

  @Test
  void should_report_empty_when_no_orders_exist() {
    OperationsSnapshot empty =
        new OperationsSnapshot(0, 0, 0, 0, 0, Money.zero(COP), List.of(), 5, 20);
    when(operationsQueryPort.load(any(), any(), any(), any(), any(), any())).thenReturn(empty);
    when(operationsQueryPort.loadLaborCost(any(), any())).thenReturn(Money.zero(COP));

    OperationsReport report = service().execute("monthly", "2027-02", "2027-02");

    assertEquals("EMPTY", report.dataCompleteness());
    assertEquals("0.00", report.revPash().value().amount().toPlainString());
    assertEquals(1, report.notes().size());
  }
}
