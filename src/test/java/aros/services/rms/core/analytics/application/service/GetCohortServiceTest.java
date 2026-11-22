/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.CohortReport;
import aros.services.rms.core.analytics.domain.port.out.CohortQueryPort;
import aros.services.rms.core.analytics.domain.port.out.CohortQueryPort.CohortOrderRow;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link GetCohortService}.
 *
 * <p><b>Feature:</b> Customer cohort report: new versus recurring visits, retention per bucket and
 * ticket sizing.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Golden G4: 30 orders with 12 fingerprints seen in the previous 90 days → 18 new, 12
 *       recurring and a 40.00% recurring rate
 *   <li>Average ticket is computed per order and per cover
 *   <li>Phone plus table fallback yields {@code PHONE_OR_TABLE} and {@code PARTIAL}
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks {@link CohortQueryPort} with history and in-period visits
 *   <li>Executes the service for the 2027-01 monthly bucket
 *   <li>Asserts the overall metrics and the single cohort window row
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class GetCohortServiceTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Mock private CohortQueryPort cohortQueryPort;
  @Mock private CurrencyProvider currencyProvider;

  @Test
  void should_match_golden_g4_cohort() {
    when(currencyProvider.getCurrency()).thenReturn(COP);
    when(cohortQueryPort.findOrders(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(rows());

    CohortReport report =
        new GetCohortService(cohortQueryPort, currencyProvider)
            .execute("monthly", "2027-01", "2027-01");

    assertNotNull(report);
    assertEquals(18, report.newClients());
    assertEquals(12, report.recurringClients());
    assertEquals(30, report.totalOrders());
    assertEquals("40.00", report.recurringRatePct().toPlainString());
    assertEquals("50000.00", report.averageTicketPerOrder().amount().toPlainString());
    assertEquals("25000.00", report.averageTicketPerCover().amount().toPlainString());
    assertEquals("PHONE_OR_TABLE", report.fingerprintStrategy());
    assertEquals("PARTIAL", report.dataCompleteness());
    assertEquals(1, report.cohorts().size());
    assertEquals("2027-01", report.cohorts().get(0).key());
    assertEquals(18, report.cohorts().get(0).newClients());
    assertEquals(12, report.cohorts().get(0).recurringClients());
  }

  private static List<CohortOrderRow> rows() {
    List<CohortOrderRow> rows = new ArrayList<>();
    // 12 history visits, 10 days before the period.
    for (int i = 0; i < 12; i++) {
      rows.add(
          new CohortOrderRow(
              (long) i,
              LocalDateTime.of(2026, 12, 20, 19, 0),
              "phone-h-" + i,
              true,
              2,
              new Money(new BigDecimal("50000.00"), COP)));
    }
    // 12 recurring visits in the period (same fingerprints).
    for (int i = 0; i < 12; i++) {
      rows.add(
          new CohortOrderRow(
              (long) (100 + i),
              LocalDateTime.of(2027, 1, 15, 19, 0),
              "phone-h-" + i,
              true,
              2,
              new Money(new BigDecimal("50000.00"), COP)));
    }
    // 18 new visits: 8 with phone, 10 table-only.
    for (int i = 0; i < 18; i++) {
      boolean phone = i < 8;
      rows.add(
          new CohortOrderRow(
              (long) (200 + i),
              LocalDateTime.of(2027, 1, 16, 19, 0),
              phone ? "phone-n-" + i : "table:" + (i + 1),
              phone,
              2,
              new Money(new BigDecimal("50000.00"), COP)));
    }
    return rows;
  }
}
