/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.mapper;

import aros.services.rms.core.analytics.domain.MenuEngineeringReport;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.CacheStatus;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MedianInfo;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.PeriodInfo;
import aros.services.rms.core.analytics.infrastructure.rest.dto.MenuEngineeringReportResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.MenuEngineeringReportResponse.CacheStatusResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.MenuEngineeringReportResponse.MedianResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.MenuEngineeringReportResponse.MenuItemResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.MenuEngineeringReportResponse.PeriodResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.MoneyDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Maps domain {@link MenuEngineeringReport} to REST {@link MenuEngineeringReportResponse}. */
@Component
public class MenuEngineeringReportMapper {

  /**
   * Maps a domain menu engineering report to its response DTO.
   *
   * @param report the domain report
   * @return the response DTO
   */
  public MenuEngineeringReportResponse toResponse(MenuEngineeringReport report) {
    if (report == null) {
      return null;
    }
    return MenuEngineeringReportResponse.builder()
        .period(toPeriod(report.period()))
        .median(toMedian(report.median()))
        .items(toItemList(report.items()))
        .cacheStatus(toCacheStatus(report.cacheStatus()))
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

  private MedianResponse toMedian(MedianInfo median) {
    if (median == null) {
      return null;
    }
    return MedianResponse.builder()
        .volume(median.volume())
        .margin(toMoneyDto(median.margin()))
        .build();
  }

  private List<MenuItemResponse> toItemList(List<MenuItemSummary> items) {
    if (items == null) {
      return List.of();
    }
    return items.stream().map(this::toItem).collect(Collectors.toList());
  }

  private MenuItemResponse toItem(MenuItemSummary item) {
    return MenuItemResponse.builder()
        .productId(item.productId())
        .productName(item.productName())
        .categoryId(item.categoryId())
        .categoryName(item.categoryName())
        .unitsSold(item.unitsSold())
        .revenue(toMoneyDto(item.revenue()))
        .recipeCost(toMoneyDto(item.recipeCost()))
        .grossProfitPerUnit(toMoneyDto(item.grossProfitPerUnit()))
        .totalContribution(toMoneyDto(item.totalContribution()))
        .quadrant(item.quadrant().name())
        .build();
  }

  private CacheStatusResponse toCacheStatus(CacheStatus cacheStatus) {
    if (cacheStatus == null) {
      return null;
    }
    return CacheStatusResponse.builder()
        .lastRefreshedAt(cacheStatus.lastRefreshedAt().toString())
        .sourceVersion(cacheStatus.sourceVersion())
        .ttlSeconds(cacheStatus.ttlSeconds())
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
