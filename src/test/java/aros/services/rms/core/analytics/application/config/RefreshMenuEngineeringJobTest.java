/* (C) 2026 */

package aros.services.rms.core.analytics.application.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import aros.services.rms.core.analytics.domain.port.in.RefreshMenuEngineeringUseCase;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RefreshMenuEngineeringJob}. Verifies the scheduled job refreshes all 13
 * months.
 */
@ExtendWith(MockitoExtension.class)
class RefreshMenuEngineeringJobTest {

  @Mock private RefreshMenuEngineeringUseCase refreshUseCase;

  @Test
  void shouldRefresh13Times() {
    RefreshMenuEngineeringJob job = new RefreshMenuEngineeringJob(refreshUseCase);

    job.refreshLast13Months();

    // Must call refresh exactly 13 times (once per month)
    verify(refreshUseCase, times(13)).refresh(eq("monthly"), anyString());
  }

  @Test
  void shouldRefreshCurrentMonth() {
    RefreshMenuEngineeringJob job = new RefreshMenuEngineeringJob(refreshUseCase);

    job.refreshLast13Months();

    // Current month is always included
    String current = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    verify(refreshUseCase).refresh(eq("monthly"), eq(current));
  }

  @Test
  void shouldRefreshOldestMonth() {
    RefreshMenuEngineeringJob job = new RefreshMenuEngineeringJob(refreshUseCase);

    job.refreshLast13Months();

    // 12 months ago (0-indexed: current minus 12)
    String oldest = YearMonth.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyy-MM"));
    verify(refreshUseCase).refresh(eq("monthly"), eq(oldest));
  }
}
