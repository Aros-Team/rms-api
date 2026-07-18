/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.adapter;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.domain.port.out.MonthlyFinancialSummaryRepositoryPort;
import aros.services.rms.core.analytics.infrastructure.persistence.entity.MonthlyFinancialSummaryEntity;
import aros.services.rms.core.analytics.infrastructure.persistence.mapper.MonthlyFinancialSummaryMapper;
import aros.services.rms.core.analytics.infrastructure.persistence.repository.JpaMonthlyFinancialSummaryRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA-backed adapter for monthly financial summary persistence. */
@Component
@RequiredArgsConstructor
public class MonthlyFinancialSummaryRepositoryAdapter
    implements MonthlyFinancialSummaryRepositoryPort {

  private final JpaMonthlyFinancialSummaryRepository jpaRepo;
  private final MonthlyFinancialSummaryMapper mapper;

  /** {@inheritDoc} */
  @Override
  public Optional<MonthlyFinancialSummary> findByPeriodKeyAndBucket(
      String periodKey, String bucket) {
    return jpaRepo.findByPeriodKeyAndBucket(periodKey, bucket).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void upsert(MonthlyFinancialSummary summary) {
    // Query-then-save pattern: check if row exists, update or create
    Optional<MonthlyFinancialSummaryEntity> existing =
        jpaRepo.findByPeriodKeyAndBucket(summary.getPeriodKey(), summary.getBucket());

    if (existing.isPresent()) {
      MonthlyFinancialSummaryEntity entity = existing.get();
      MonthlyFinancialSummaryEntity updated = merge(entity, summary);
      jpaRepo.save(updated);
    } else {
      MonthlyFinancialSummaryEntity entity = mapper.toEntity(summary);
      jpaRepo.save(entity);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<MonthlyFinancialSummary> findByBucketAndPeriodKeyBetween(
      String bucket, String fromKey, String toKey) {
    return jpaRepo.findByBucketAndPeriodKeyBetween(bucket, fromKey, toKey).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  private MonthlyFinancialSummaryEntity merge(
      MonthlyFinancialSummaryEntity entity, MonthlyFinancialSummary summary) {
    entity.setPeriodKey(summary.getPeriodKey());
    entity.setBucket(summary.getBucket());
    entity.setNetSales(summary.getNetSales().amount());
    entity.setGrossSales(summary.getGrossSales().amount());
    entity.setDiscounts(summary.getDiscounts().amount());
    entity.setComped(summary.getComped().amount());
    entity.setCogsFood(summary.getCogsFood().amount());
    entity.setCogsBeverage(summary.getCogsBeverage().amount());
    entity.setCogsAlcohol(summary.getCogsAlcohol().amount());
    entity.setCogsOther(summary.getCogsOther().amount());
    entity.setFoodCogsPct(summary.getFoodCogsPct());
    entity.setLaborFoh(summary.getLaborFoh().amount());
    entity.setLaborBoh(summary.getLaborBoh().amount());
    entity.setLaborTotal(summary.getLaborTotal().amount());
    entity.setLaborPct(summary.getLaborPct());
    entity.setPrimeCost(summary.getPrimeCost().amount());
    entity.setPrimeCostPct(summary.getPrimeCostPct());
    entity.setGrossProfitPct(summary.getGrossProfitPct());
    entity.setNetProfitPct(summary.getNetProfitPct());
    entity.setDataCompleteness(summary.getDataCompleteness());
    return entity;
  }
}
