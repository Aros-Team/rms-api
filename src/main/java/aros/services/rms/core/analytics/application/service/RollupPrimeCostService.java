/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.AnalyticsPeriods;
import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.domain.port.in.RollupPrimeCostUseCase;
import aros.services.rms.core.analytics.domain.port.out.MonthlyFinancialSummaryRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rolls daily prime cost rows up into weekly, monthly and yearly buckets.
 *
 * <p>The rollup is idempotent: each target bucket row is rebuilt from its daily rows and upserted
 * on {@code (period_key, bucket)}, so re-running the same range produces identical values.
 */
@Service
@RequiredArgsConstructor
public class RollupPrimeCostService implements RollupPrimeCostUseCase {

  private static final BigDecimal HUNDRED = new BigDecimal("100");

  private final MonthlyFinancialSummaryRepositoryPort summaryRepositoryPort;
  private final CurrencyProvider currencyProvider;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public int rollup(LocalDate from, LocalDate to) {
    if (to.isBefore(from)) {
      throw new aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsRangeException(
          "'to' no puede ser anterior a 'from'");
    }
    List<MonthlyFinancialSummary> daily =
        summaryRepositoryPort.findByBucketAndPeriodKeyBetween(
            "daily", from.toString(), to.toString());
    if (daily.isEmpty()) {
      return 0;
    }

    Map<String, List<MonthlyFinancialSummary>> weekly = new LinkedHashMap<>();
    Map<String, List<MonthlyFinancialSummary>> monthly = new LinkedHashMap<>();
    Map<String, List<MonthlyFinancialSummary>> yearly = new LinkedHashMap<>();
    for (MonthlyFinancialSummary row : daily) {
      LocalDate date = LocalDate.parse(row.getPeriodKey());
      weekly
          .computeIfAbsent(AnalyticsPeriods.keyFor("weekly", date), key -> new ArrayList<>())
          .add(row);
      monthly
          .computeIfAbsent(AnalyticsPeriods.keyFor("monthly", date), key -> new ArrayList<>())
          .add(row);
      yearly
          .computeIfAbsent(AnalyticsPeriods.keyFor("yearly", date), key -> new ArrayList<>())
          .add(row);
    }

    int written = 0;
    written += write("weekly", weekly, 7);
    written += write("monthly", monthly, null);
    written += write("yearly", yearly, null);
    return written;
  }

  private int write(
      String bucket, Map<String, List<MonthlyFinancialSummary>> groups, Integer expectedDays) {
    int written = 0;
    for (Map.Entry<String, List<MonthlyFinancialSummary>> entry : groups.entrySet()) {
      String key = entry.getKey();
      List<MonthlyFinancialSummary> rows = entry.getValue();
      int expected = expectedDays != null ? expectedDays : expectedDaysFor(bucket, key);
      boolean coverageComplete = rows.size() >= expected;
      boolean allFull = rows.stream().allMatch(row -> "FULL".equals(row.getDataCompleteness()));
      String completeness;
      if (rows.isEmpty()) {
        completeness = "EMPTY";
      } else if (coverageComplete && allFull) {
        completeness = "FULL";
      } else {
        completeness = "PARTIAL";
      }
      summaryRepositoryPort.upsert(aggregate(key, bucket, rows, completeness));
      written++;
    }
    return written;
  }

  private static int expectedDaysFor(String bucket, String key) {
    return switch (bucket) {
      case "monthly" -> YearMonth.parse(key).lengthOfMonth();
      case "yearly" -> LocalDate.of(Integer.parseInt(key), 1, 1).lengthOfYear();
      default -> 7;
    };
  }

  private MonthlyFinancialSummary aggregate(
      String periodKey, String bucket, List<MonthlyFinancialSummary> rows, String completeness) {
    Currency currency = currencyProvider.getCurrency();
    Money net = Money.zero(currency);
    Money gross = Money.zero(currency);
    Money discounts = Money.zero(currency);
    Money comped = Money.zero(currency);
    Money cogsFood = Money.zero(currency);
    Money cogsBeverage = Money.zero(currency);
    Money cogsAlcohol = Money.zero(currency);
    Money cogsOther = Money.zero(currency);
    Money laborFoh = Money.zero(currency);
    Money laborBoh = Money.zero(currency);
    Money laborTotal = Money.zero(currency);

    for (MonthlyFinancialSummary row : rows) {
      net = net.plus(nullSafe(row.getNetSales(), currency));
      gross = gross.plus(nullSafe(row.getGrossSales(), currency));
      discounts = discounts.plus(nullSafe(row.getDiscounts(), currency));
      comped = comped.plus(nullSafe(row.getComped(), currency));
      cogsFood = cogsFood.plus(nullSafe(row.getCogsFood(), currency));
      cogsBeverage = cogsBeverage.plus(nullSafe(row.getCogsBeverage(), currency));
      cogsAlcohol = cogsAlcohol.plus(nullSafe(row.getCogsAlcohol(), currency));
      cogsOther = cogsOther.plus(nullSafe(row.getCogsOther(), currency));
      laborFoh = laborFoh.plus(nullSafe(row.getLaborFoh(), currency));
      laborBoh = laborBoh.plus(nullSafe(row.getLaborBoh(), currency));
      laborTotal = laborTotal.plus(nullSafe(row.getLaborTotal(), currency));
    }

    Money totalCogs = cogsFood.plus(cogsBeverage).plus(cogsAlcohol).plus(cogsOther);
    Money primeCost = totalCogs.plus(laborTotal);

    return MonthlyFinancialSummary.builder()
        .periodKey(periodKey)
        .bucket(bucket)
        .netSales(net)
        .grossSales(gross)
        .discounts(discounts)
        .comped(comped)
        .cogsFood(cogsFood)
        .cogsBeverage(cogsBeverage)
        .cogsAlcohol(cogsAlcohol)
        .cogsOther(cogsOther)
        .foodCogsPct(pct(cogsFood, net))
        .laborFoh(laborFoh)
        .laborBoh(laborBoh)
        .laborTotal(laborTotal)
        .laborPct(pct(laborTotal, net))
        .primeCost(primeCost)
        .primeCostPct(pct(primeCost, net))
        .grossProfitPct(pct(net.minus(totalCogs), net))
        .netProfitPct(pct(net.minus(totalCogs).minus(laborTotal), net))
        .dataCompleteness(completeness)
        .build();
  }

  private static Money nullSafe(Money money, Currency currency) {
    return money == null ? Money.zero(currency) : money;
  }

  private static BigDecimal pct(Money numerator, Money denominator) {
    if (denominator.isZero()) {
      return BigDecimal.ZERO;
    }
    return numerator
        .amount()
        .multiply(HUNDRED)
        .divide(denominator.amount(), 2, RoundingMode.HALF_UP);
  }
}
