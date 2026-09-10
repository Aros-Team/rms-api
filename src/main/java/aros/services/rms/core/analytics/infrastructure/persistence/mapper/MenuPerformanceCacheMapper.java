/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.mapper;

import aros.services.rms.core.analytics.domain.BcgQuadrant;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.infrastructure.persistence.entity.MenuPerformanceCacheEntity;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import org.springframework.stereotype.Component;

/** Maps between {@link MenuPerformanceCacheEntity} and {@link MenuItemSummary} domain objects. */
@Component
public class MenuPerformanceCacheMapper {

  private final CurrencyProvider currencyProvider;

  /**
   * Creates a new instance.
   *
   * @param currencyProvider the system currency provider
   */
  public MenuPerformanceCacheMapper(CurrencyProvider currencyProvider) {
    this.currencyProvider = currencyProvider;
  }

  /**
   * Maps a persistence entity to a domain menu item summary.
   *
   * @param entity the entity
   * @return the domain record with Money wrappers
   */
  public MenuItemSummary toDomain(MenuPerformanceCacheEntity entity) {
    var currency = currencyProvider.getCurrency();
    return new MenuItemSummary(
        entity.getProductId(),
        entity.getProductName(),
        entity.getCategoryId(),
        entity.getCategoryName(),
        entity.getUnitsSold(),
        new Money(entity.getRevenue(), currency),
        new Money(entity.getRecipeCost(), currency),
        new Money(entity.getAvgOptionCost(), currency),
        new Money(entity.getEffectiveCost(), currency),
        new Money(entity.getGrossProfitPerUnit(), currency),
        new Money(entity.getTotalContribution(), currency),
        BcgQuadrant.valueOf(entity.getQuadrant()));
  }

  /**
   * Maps a domain menu item summary + metadata to a persistence entity.
   *
   * @param item the domain summary
   * @param bucket the time bucket
   * @param periodKey the period key
   * @param sourceVersion the source version
   * @return the entity with raw BigDecimal columns
   */
  public MenuPerformanceCacheEntity toEntity(
      MenuItemSummary item, String bucket, String periodKey, String sourceVersion) {
    return MenuPerformanceCacheEntity.builder()
        .productId(item.productId())
        .productName(item.productName())
        .categoryId(item.categoryId())
        .categoryName(item.categoryName())
        .periodKey(periodKey)
        .bucket(bucket)
        .unitsSold(item.unitsSold())
        .revenue(item.revenue().amount())
        .recipeCost(item.recipeCost().amount())
        .avgOptionCost(item.avgOptionCost().amount())
        .effectiveCost(item.effectiveCost().amount())
        .grossProfitPerUnit(item.grossProfitPerUnit().amount())
        .totalContribution(item.totalContribution().amount())
        .quadrant(item.quadrant().name())
        .sourceVersion(sourceVersion)
        .build();
  }
}
