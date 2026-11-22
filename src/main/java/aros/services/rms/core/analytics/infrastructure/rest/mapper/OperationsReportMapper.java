/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.mapper;

import aros.services.rms.core.analytics.domain.OperationsReport;
import aros.services.rms.core.analytics.domain.OperationsReport.DayPartTurnover;
import aros.services.rms.core.analytics.domain.OperationsReport.LaborShare;
import aros.services.rms.core.analytics.domain.OperationsReport.PeriodInfo;
import aros.services.rms.core.analytics.domain.OperationsReport.RevPash;
import aros.services.rms.core.analytics.domain.OperationsReport.TableTurnover;
import aros.services.rms.core.analytics.infrastructure.rest.dto.OperationsReportResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.OperationsReportResponse.DayPartTurnoverResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.OperationsReportResponse.LaborResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.OperationsReportResponse.PeriodResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.OperationsReportResponse.RevPashResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.OperationsReportResponse.TableTurnoverResponse;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Maps a domain {@link OperationsReport} to its REST DTO. */
@Component
public class OperationsReportMapper {

  /**
   * Converts a domain report to its response DTO.
   *
   * @param report the domain report
   * @return the response DTO, or null
   */
  public OperationsReportResponse toResponse(OperationsReport report) {
    if (report == null) {
      return null;
    }
    return OperationsReportResponse.builder()
        .period(toPeriod(report.period()))
        .revPash(toRevPash(report.revPash()))
        .tableTurnover(toTurnover(report.tableTurnover()))
        .labor(toLabor(report.labor()))
        .covers(report.covers())
        .orders(report.orders())
        .avgOccupancyMinutes(report.avgOccupancyMinutes())
        .totalOccupiedSeatMinutes(report.totalOccupiedSeatMinutes())
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

  private RevPashResponse toRevPash(RevPash revPash) {
    if (revPash == null) {
      return null;
    }
    return RevPashResponse.builder()
        .value(MoneyDto.of(revPash.value()))
        .totalNetSales(MoneyDto.of(revPash.totalNetSales()))
        .totalAvailableSeatHours(revPash.totalAvailableSeatHours())
        .build();
  }

  private TableTurnoverResponse toTurnover(TableTurnover turnover) {
    if (turnover == null) {
      return null;
    }
    List<DayPartTurnoverResponse> byDayPart =
        turnover.byDayPart() == null
            ? List.of()
            : turnover.byDayPart().stream().map(this::toDayPart).collect(Collectors.toList());
    return TableTurnoverResponse.builder().overall(turnover.overall()).byDayPart(byDayPart).build();
  }

  private DayPartTurnoverResponse toDayPart(DayPartTurnover dayPart) {
    return DayPartTurnoverResponse.builder()
        .dayPart(dayPart.dayPart())
        .turns(dayPart.turns())
        .covers(dayPart.covers())
        .tables(dayPart.tables())
        .build();
  }

  private LaborResponse toLabor(LaborShare labor) {
    if (labor == null) {
      return null;
    }
    return LaborResponse.builder()
        .totalCost(MoneyDto.of(labor.totalCost()))
        .sharePct(labor.sharePct())
        .build();
  }
}
