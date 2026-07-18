/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.mapper;

import aros.services.rms.core.analytics.domain.PrimeCostReport;
import aros.services.rms.core.analytics.domain.PrimeCostReport.CogsBreakdown;
import aros.services.rms.core.analytics.domain.PrimeCostReport.CogsCategory;
import aros.services.rms.core.analytics.domain.PrimeCostReport.LaborArea;
import aros.services.rms.core.analytics.domain.PrimeCostReport.LaborBreakdown;
import aros.services.rms.core.analytics.domain.PrimeCostReport.Margins;
import aros.services.rms.core.analytics.domain.PrimeCostReport.Period;
import aros.services.rms.core.analytics.domain.PrimeCostReport.PrimeCostSeries;
import aros.services.rms.core.analytics.infrastructure.rest.dto.MoneyDto;
import aros.services.rms.core.analytics.infrastructure.rest.dto.PrimeCostReportResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.PrimeCostReportResponse.CogsBreakdownResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.PrimeCostReportResponse.CogsCategoryResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.PrimeCostReportResponse.LaborAreaResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.PrimeCostReportResponse.LaborBreakdownResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.PrimeCostReportResponse.MarginsResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.PrimeCostReportResponse.PeriodResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.PrimeCostReportResponse.PrimeCostSeriesResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Maps domain {@link PrimeCostReport} to REST {@link PrimeCostReportResponse}. */
@Component
public class PrimeCostReportMapper {

  /** Maps a domain prime cost report to its response DTO. */
  public PrimeCostReportResponse toResponse(PrimeCostReport report) {
    if (report == null) {
      return null;
    }
    return PrimeCostReportResponse.builder()
        .period(toPeriod(report.period()))
        .series(toSeriesList(report.series()))
        .dataCompleteness(report.dataCompleteness())
        .notes(report.notes())
        .build();
  }

  private PeriodResponse toPeriod(Period period) {
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

  private List<PrimeCostSeriesResponse> toSeriesList(List<PrimeCostSeries> series) {
    if (series == null) {
      return List.of();
    }
    return series.stream().map(this::toSeries).collect(Collectors.toList());
  }

  private PrimeCostSeriesResponse toSeries(PrimeCostSeries s) {
    return PrimeCostSeriesResponse.builder()
        .key(s.key())
        .netSales(toMoneyDto(s.netSales()))
        .grossSales(toMoneyDto(s.grossSales()))
        .discounts(toMoneyDto(s.discounts()))
        .comped(toMoneyDto(s.comped()))
        .cogs(toCogsBreakdown(s.cogs()))
        .labor(toLaborBreakdown(s.labor()))
        .primeCost(toMoneyDto(s.primeCost()))
        .primeCostPct(s.primeCostPct())
        .margins(toMargins(s.margins()))
        .dataCompleteness(s.dataCompleteness())
        .build();
  }

  private CogsBreakdownResponse toCogsBreakdown(CogsBreakdown cogs) {
    if (cogs == null) {
      return null;
    }
    return CogsBreakdownResponse.builder()
        .total(toMoneyDto(cogs.total()))
        .byCategory(toCogsCategoryList(cogs.byCategory()))
        .build();
  }

  private List<CogsCategoryResponse> toCogsCategoryList(List<CogsCategory> cats) {
    if (cats == null) {
      return List.of();
    }
    return cats.stream()
        .map(
            c ->
                CogsCategoryResponse.builder()
                    .category(c.category())
                    .amount(toMoneyDto(c.amount()))
                    .pct(c.pct())
                    .build())
        .collect(Collectors.toList());
  }

  private LaborBreakdownResponse toLaborBreakdown(LaborBreakdown labor) {
    if (labor == null) {
      return null;
    }
    return LaborBreakdownResponse.builder()
        .total(toMoneyDto(labor.total()))
        .byArea(toLaborAreaList(labor.byArea()))
        .build();
  }

  private List<LaborAreaResponse> toLaborAreaList(List<LaborArea> areas) {
    if (areas == null) {
      return List.of();
    }
    return areas.stream()
        .map(
            a ->
                LaborAreaResponse.builder()
                    .area(a.area())
                    .amount(toMoneyDto(a.amount()))
                    .pct(a.pct())
                    .build())
        .collect(Collectors.toList());
  }

  private MarginsResponse toMargins(Margins margins) {
    if (margins == null) {
      return null;
    }
    return MarginsResponse.builder()
        .grossProfitPct(margins.grossProfitPct())
        .netProfitPct(margins.netProfitPct())
        .build();
  }

  private MoneyDto toMoneyDto(aros.services.rms.core.common.money.domain.Money money) {
    if (money == null) {
      return null;
    }
    return MoneyDto.builder()
        .amount(money.amount().toPlainString())
        .currency(money.currency().getCurrencyCode())
        .build();
  }
}
