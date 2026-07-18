/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.domain.port.out.MonthlyFinancialSummaryRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RefreshPrimeCostService}. Verifies COGS, labor, and net sales aggregation
 * math.
 */
@ExtendWith(MockitoExtension.class)
class RefreshPrimeCostServiceTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final LocalDate TEST_DATE = LocalDate.of(2026, 7, 17);

  @Mock private EntityManager entityManager;
  @Mock private MonthlyFinancialSummaryRepositoryPort summaryRepo;
  @Mock private Query query;

  private RefreshPrimeCostService service() {
    return new RefreshPrimeCostService(entityManager, summaryRepo);
  }

  // ---------------------------------------------------------------------------
  // H-01: happy path — all aggregations produce correct values
  // ---------------------------------------------------------------------------

  @Test
  void should_compute_prime_cost_for_date() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    // COGS queries return per-category amounts
    when(query.setParameter(anyString(), any())).thenAnswer(invocation -> query);
    // First 4 calls are COGS per category (FOOD, BEVERAGE, ALCOHOL, OTHER)
    // Then 2 calls for sales (net + gross)
    // Then 2 calls for labor (FOH + BOH)
    // Result order: cogsFood, cogsBeverage, cogsAlcohol, cogsOther, netSales, grossSales, laborFoh,
    // laborBoh
    when(query.getSingleResult())
        .thenReturn(new BigDecimal("30000000.00")) // cogsFood
        .thenReturn(new BigDecimal("8000000.00")) // cogsBeverage
        .thenReturn(new BigDecimal("4000000.00")) // cogsAlcohol
        .thenReturn(new BigDecimal("0.00")) // cogsOther
        .thenReturn(125000000.00) // netSales (Double from SUM)
        .thenReturn(125000000.00) // grossSales
        .thenReturn(new BigDecimal("18000000.00")) // laborFoh
        .thenReturn(new BigDecimal("17000000.00")); // laborBoh

    RefreshPrimeCostService svc = service();
    MonthlyFinancialSummary result = svc.refreshForDate(TEST_DATE);

    assertNotNull(result);
    assertEquals("2026-07-17", result.getPeriodKey());
    assertEquals("daily", result.getBucket());

    // COGS
    assertEquals(new Money(new BigDecimal("30000000.00"), COP), result.getCogsFood());
    assertEquals(new Money(new BigDecimal("8000000.00"), COP), result.getCogsBeverage());
    assertEquals(new Money(new BigDecimal("4000000.00"), COP), result.getCogsAlcohol());
    assertEquals(new Money(new BigDecimal("0.00"), COP), result.getCogsOther());

    // Sales
    assertEquals(new Money(new BigDecimal("125000000.00"), COP), result.getNetSales());
    assertEquals(new Money(new BigDecimal("125000000.00"), COP), result.getGrossSales());

    // Labor
    assertEquals(new Money(new BigDecimal("18000000.00"), COP), result.getLaborFoh());
    assertEquals(new Money(new BigDecimal("17000000.00"), COP), result.getLaborBoh());
    assertEquals(new Money(new BigDecimal("35000000.00"), COP), result.getLaborTotal());

    // Prime cost = 42M COGS + 35M labor = 77M
    assertEquals(new Money(new BigDecimal("77000000.00"), COP), result.getPrimeCost());

    // Percentages: primeCostPct = 77M/125M * 100 = 61.60
    assertEquals(0, new BigDecimal("61.60").compareTo(result.getPrimeCostPct()));

    verify(summaryRepo).upsert(any(MonthlyFinancialSummary.class));
  }

  // ---------------------------------------------------------------------------
  // H-02: empty data — returns zeroed summary with PARTIAL completeness
  // ---------------------------------------------------------------------------

  @Test
  void should_return_zeroed_summary_when_no_data() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenAnswer(invocation -> query);
    // All aggregations return zero
    when(query.getSingleResult())
        .thenReturn(BigDecimal.ZERO) // cogsFood
        .thenReturn(BigDecimal.ZERO) // cogsBeverage
        .thenReturn(BigDecimal.ZERO) // cogsAlcohol
        .thenReturn(BigDecimal.ZERO) // cogsOther
        .thenReturn(0.0) // netSales
        .thenReturn(0.0) // grossSales
        .thenReturn(BigDecimal.ZERO) // laborFoh
        .thenReturn(BigDecimal.ZERO); // laborBoh

    RefreshPrimeCostService svc = service();
    MonthlyFinancialSummary result = svc.refreshForDate(TEST_DATE);

    assertEquals(Money.zero(COP), result.getNetSales());
    assertEquals(Money.zero(COP), result.getPrimeCost());
    assertEquals("PARTIAL", result.getDataCompleteness());
  }

  // ---------------------------------------------------------------------------
  // H-03: net_sales = 0 → prime_cost_pct = 0 (no division by zero)
  // ---------------------------------------------------------------------------

  @Test
  void should_handle_zero_net_sales_gracefully() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenAnswer(invocation -> query);
    when(query.getSingleResult())
        .thenReturn(BigDecimal.ZERO) // cogsFood
        .thenReturn(BigDecimal.ZERO) // cogsBeverage
        .thenReturn(BigDecimal.ZERO) // cogsAlcohol
        .thenReturn(BigDecimal.ZERO) // cogsOther
        .thenReturn(0.0) // netSales → 0
        .thenReturn(0.0) // grossSales → 0
        .thenReturn(BigDecimal.ZERO) // laborFoh
        .thenReturn(BigDecimal.ZERO); // laborBoh

    RefreshPrimeCostService svc = service();
    MonthlyFinancialSummary result = svc.refreshForDate(TEST_DATE);

    assertEquals(BigDecimal.ZERO, result.getPrimeCostPct());
    assertEquals(BigDecimal.ZERO, result.getGrossProfitPct());
  }
}
