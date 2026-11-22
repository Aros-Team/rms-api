/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.mapper;

import aros.services.rms.core.analytics.domain.CohortReport;
import aros.services.rms.core.analytics.domain.CohortReport.CohortWindow;
import aros.services.rms.core.analytics.domain.CohortReport.PeriodInfo;
import aros.services.rms.core.analytics.infrastructure.rest.dto.CohortReportResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.CohortReportResponse.CohortWindowResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.CohortReportResponse.PeriodResponse;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Maps a domain {@link CohortReport} to its REST DTO. */
@Component
public class CohortReportMapper {

  /**
   * Converts a domain report to its response DTO.
   *
   * @param report the domain report
   * @return the response DTO, or null
   */
  public CohortReportResponse toResponse(CohortReport report) {
    if (report == null) {
      return null;
    }
    return CohortReportResponse.builder()
        .period(toPeriod(report.period()))
        .newClients(report.newClients())
        .recurringClients(report.recurringClients())
        .totalOrders(report.totalOrders())
        .recurringRatePct(report.recurringRatePct())
        .averageTicketPerOrder(MoneyDto.of(report.averageTicketPerOrder()))
        .averageTicketPerCover(MoneyDto.of(report.averageTicketPerCover()))
        .fingerprintStrategy(report.fingerprintStrategy())
        .cohortWindowDays(report.cohortWindowDays())
        .cohorts(toCohorts(report.cohorts()))
        .dataCompleteness(report.dataCompleteness())
        .notes(report.notes())
        .build();
  }

  private PeriodResponse toPeriod(PeriodInfo period) {
    if (period == null) {
      return null;
    }
    return PeriodResponse.builder()
        .bucket(period.bucket())
        .from(period.from())
        .to(period.to())
        .keys(period.keys())
        .build();
  }

  private List<CohortWindowResponse> toCohorts(List<CohortWindow> cohorts) {
    if (cohorts == null) {
      return List.of();
    }
    return cohorts.stream()
        .map(
            cohort ->
                CohortWindowResponse.builder()
                    .key(cohort.key())
                    .newClients(cohort.newClients())
                    .recurringClients(cohort.recurringClients())
                    .totalOrders(cohort.totalOrders())
                    .recurringRatePct(cohort.recurringRatePct())
                    .build())
        .collect(Collectors.toList());
  }
}
