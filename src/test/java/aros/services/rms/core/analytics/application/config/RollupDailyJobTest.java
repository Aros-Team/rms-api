/* (C) 2026 */

package aros.services.rms.core.analytics.application.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.domain.port.in.RefreshPrimeCostUseCase;
import aros.services.rms.core.common.money.domain.Money;
import java.time.LocalDate;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RollupDailyJob}. Verifies the scheduled job calls refreshForDate with
 * yesterday's date.
 */
@ExtendWith(MockitoExtension.class)
class RollupDailyJobTest {

  @Mock private RefreshPrimeCostUseCase refreshPrimeCost;

  @Test
  void should_refresh_yesterday_data() {
    RollupDailyJob job = new RollupDailyJob(refreshPrimeCost);
    LocalDate today = LocalDate.now();
    LocalDate expectedYesterday = today.minusDays(1);

    MonthlyFinancialSummary mockSummary =
        MonthlyFinancialSummary.builder()
            .periodKey(expectedYesterday.toString())
            .bucket("daily")
            .netSales(Money.zero(Currency.getInstance("COP")))
            .build();

    when(refreshPrimeCost.refreshForDate(any())).thenReturn(mockSummary);

    job.rollupYesterday();

    ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
    verify(refreshPrimeCost).refreshForDate(captor.capture());
    assert captor.getValue().equals(expectedYesterday)
        : "Expected " + expectedYesterday + " but got " + captor.getValue();
  }
}
