/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.adapter;

import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.analytics.infrastructure.persistence.entity.MenuPerformanceCacheEntity;
import aros.services.rms.core.analytics.infrastructure.persistence.mapper.MenuPerformanceCacheMapper;
import aros.services.rms.core.analytics.infrastructure.persistence.repository.JpaMenuPerformanceCacheRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA-backed adapter for menu performance cache persistence. */
@Component
@RequiredArgsConstructor
public class MenuPerformanceCacheRepositoryAdapter implements MenuEngineeringCacheRepositoryPort {

  private final JpaMenuPerformanceCacheRepository jpaRepo;
  private final MenuPerformanceCacheMapper mapper;

  /** {@inheritDoc} */
  @Override
  public List<MenuItemSummary> findByBucketAndPeriodKeyBetween(
      String bucket, String fromKey, String toKey, Long categoryId) {
    List<MenuPerformanceCacheEntity> entities;
    if (categoryId != null) {
      entities =
          jpaRepo.findByBucketAndPeriodKeyBetweenAndCategory(bucket, fromKey, toKey, categoryId);
    } else {
      entities = jpaRepo.findByBucketAndPeriodKeyBetween(bucket, fromKey, toKey);
    }
    return entities.stream().map(mapper::toDomain).collect(Collectors.toList());
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void upsert(MenuItemSummary item, String bucket, String periodKey, String sourceVersion) {
    // Query-then-save pattern
    Optional<MenuPerformanceCacheEntity> existing =
        jpaRepo.findByProductIdAndPeriodKeyAndBucket(item.productId(), periodKey, bucket);

    if (existing.isPresent()) {
      MenuPerformanceCacheEntity entity = existing.get();
      merge(entity, item, sourceVersion);
      jpaRepo.save(entity);
    } else {
      MenuPerformanceCacheEntity entity = mapper.toEntity(item, bucket, periodKey, sourceVersion);
      jpaRepo.save(entity);
    }
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void deleteByProductId(Long productId) {
    jpaRepo.deleteByProductId(productId);
  }

  /** {@inheritDoc} */
  @Override
  public String findLatestSourceVersion() {
    return jpaRepo.findLatestSourceVersion();
  }

  private void merge(
      MenuPerformanceCacheEntity entity, MenuItemSummary item, String sourceVersion) {
    entity.setProductName(item.productName());
    entity.setCategoryId(item.categoryId());
    entity.setCategoryName(item.categoryName());
    entity.setUnitsSold(item.unitsSold());
    entity.setRevenue(item.revenue().amount());
    entity.setRecipeCost(item.recipeCost().amount());
    entity.setAvgOptionCost(item.avgOptionCost().amount());
    entity.setEffectiveCost(item.effectiveCost().amount());
    entity.setGrossProfitPerUnit(item.grossProfitPerUnit().amount());
    entity.setTotalContribution(item.totalContribution().amount());
    entity.setQuadrant(item.quadrant().name());
    entity.setSourceVersion(sourceVersion);
  }
}
