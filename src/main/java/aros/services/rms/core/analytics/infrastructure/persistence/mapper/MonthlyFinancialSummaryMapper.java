/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.mapper;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.infrastructure.persistence.entity.MonthlyFinancialSummaryEntity;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Mapper between monthly financial summary persistence and domain models. */
@Component
public class MonthlyFinancialSummaryMapper {

  private final CurrencyProvider currencyProvider;

  /**
   * Creates a new instance.
   *
   * @param currencyProvider the system currency provider
   */
  public MonthlyFinancialSummaryMapper(CurrencyProvider currencyProvider) {
    this.currencyProvider = currencyProvider;
  }

  /**
   * Maps a domain {@link MonthlyFinancialSummary} to a persistence entity.
   *
   * @param domain the domain record
   * @return the persistence entity with raw BigDecimal columns
   */
  public MonthlyFinancialSummaryEntity toEntity(MonthlyFinancialSummary domain) {
    if (domain == null) {
      return null;
    }
    return MonthlyFinancialSummaryEntity.builder()
        .periodKey(domain.getPeriodKey())
        .bucket(domain.getBucket())
        .netSales(moneyToBigDecimal(domain.getNetSales()))
        .grossSales(moneyToBigDecimal(domain.getGrossSales()))
        .discounts(moneyToBigDecimal(domain.getDiscounts()))
        .comped(moneyToBigDecimal(domain.getComped()))
        .cogsFood(moneyToBigDecimal(domain.getCogsFood()))
        .cogsBeverage(moneyToBigDecimal(domain.getCogsBeverage()))
        .cogsAlcohol(moneyToBigDecimal(domain.getCogsAlcohol()))
        .cogsOther(moneyToBigDecimal(domain.getCogsOther()))
        .foodCogsPct(domain.getFoodCogsPct())
        .laborFoh(moneyToBigDecimal(domain.getLaborFoh()))
        .laborBoh(moneyToBigDecimal(domain.getLaborBoh()))
        .laborTotal(moneyToBigDecimal(domain.getLaborTotal()))
        .laborPct(domain.getLaborPct())
        .primeCost(moneyToBigDecimal(domain.getPrimeCost()))
        .primeCostPct(domain.getPrimeCostPct())
        .grossProfitPct(domain.getGrossProfitPct())
        .netProfitPct(domain.getNetProfitPct())
        .dataCompleteness(domain.getDataCompleteness())
        .build();
  }

  /**
   * Maps a {@link MonthlyFinancialSummaryEntity} to its domain representation.
   *
   * @param entity the persistence entity
   * @return the domain record with Money wrappers
   */
  public MonthlyFinancialSummary toDomain(MonthlyFinancialSummaryEntity entity) {
    if (entity == null) {
      return null;
    }
    var currency = currencyProvider.getCurrency();
    return MonthlyFinancialSummary.builder()
        .periodKey(entity.getPeriodKey())
        .bucket(entity.getBucket())
        .netSales(bigDecimalToMoney(entity.getNetSales(), currency))
        .grossSales(bigDecimalToMoney(entity.getGrossSales(), currency))
        .discounts(bigDecimalToMoney(entity.getDiscounts(), currency))
        .comped(bigDecimalToMoney(entity.getComped(), currency))
        .cogsFood(bigDecimalToMoney(entity.getCogsFood(), currency))
        .cogsBeverage(bigDecimalToMoney(entity.getCogsBeverage(), currency))
        .cogsAlcohol(bigDecimalToMoney(entity.getCogsAlcohol(), currency))
        .cogsOther(bigDecimalToMoney(entity.getCogsOther(), currency))
        .foodCogsPct(entity.getFoodCogsPct())
        .laborFoh(bigDecimalToMoney(entity.getLaborFoh(), currency))
        .laborBoh(bigDecimalToMoney(entity.getLaborBoh(), currency))
        .laborTotal(bigDecimalToMoney(entity.getLaborTotal(), currency))
        .laborPct(entity.getLaborPct())
        .primeCost(bigDecimalToMoney(entity.getPrimeCost(), currency))
        .primeCostPct(entity.getPrimeCostPct())
        .grossProfitPct(entity.getGrossProfitPct())
        .netProfitPct(entity.getNetProfitPct())
        .dataCompleteness(entity.getDataCompleteness())
        .build();
  }

  /**
   * Converts a Money domain value to its raw BigDecimal for persistence.
   *
   * @param money the money value
   * @return the raw BigDecimal amount, or zero if null
   */
  private BigDecimal moneyToBigDecimal(Money money) {
    return money == null ? BigDecimal.ZERO : money.amount();
  }

  /**
   * Converts a raw BigDecimal from the database to a Money domain value.
   *
   * @param value the raw decimal amount
   * @param currency the system currency
   * @return the Money wrapper, or zero if null
   */
  private Money bigDecimalToMoney(BigDecimal value, java.util.Currency currency) {
    return value == null ? Money.zero(currency) : new Money(value, currency);
  }
}
