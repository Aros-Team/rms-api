/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.mapper;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.infrastructure.persistence.entity.MonthlyFinancialSummaryEntity;
import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

/** MapStruct mapper between monthly financial summary persistence and domain models. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MonthlyFinancialSummaryMapper {

  Currency COP = Currency.getInstance("COP");

  /**
   * Maps a domain {@link MonthlyFinancialSummary} to a persistence entity.
   *
   * @param domain the domain record
   * @return the persistence entity with raw BigDecimal columns
   */
  @Mapping(target = "netSales", source = "netSales", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "grossSales", source = "grossSales", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "discounts", source = "discounts", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "comped", source = "comped", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "cogsFood", source = "cogsFood", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "cogsBeverage", source = "cogsBeverage", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "cogsAlcohol", source = "cogsAlcohol", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "cogsOther", source = "cogsOther", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "laborFoh", source = "laborFoh", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "laborBoh", source = "laborBoh", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "laborTotal", source = "laborTotal", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "primeCost", source = "primeCost", qualifiedByName = "moneyToBigDecimal")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  MonthlyFinancialSummaryEntity toEntity(MonthlyFinancialSummary domain);

  /**
   * Maps a {@link MonthlyFinancialSummary} entity to its domain representation.
   *
   * @param entity the persistence entity
   * @return the domain record with Money wrappers
   */
  @Mapping(target = "netSales", source = "netSales", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "grossSales", source = "grossSales", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "discounts", source = "discounts", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "comped", source = "comped", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "cogsFood", source = "cogsFood", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "cogsBeverage", source = "cogsBeverage", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "cogsAlcohol", source = "cogsAlcohol", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "cogsOther", source = "cogsOther", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "laborFoh", source = "laborFoh", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "laborBoh", source = "laborBoh", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "laborTotal", source = "laborTotal", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "primeCost", source = "primeCost", qualifiedByName = "bigDecimalToMoney")
  @Mapping(target = "id", ignore = true)
  MonthlyFinancialSummary toDomain(MonthlyFinancialSummaryEntity entity);

  /**
   * Converts a Money domain value to its raw BigDecimal for persistence.
   *
   * @param money the money value
   * @return the raw BigDecimal amount, or zero if null
   */
  @Named("moneyToBigDecimal")
  default BigDecimal moneyToBigDecimal(Money money) {
    return money == null ? BigDecimal.ZERO : money.amount();
  }

  /**
   * Converts a raw BigDecimal from the database to a Money domain value.
   *
   * @param value the raw decimal amount
   * @return the Money wrapper with COP currency, or zero if null
   */
  @Named("bigDecimalToMoney")
  default Money bigDecimalToMoney(BigDecimal value) {
    return value == null ? Money.zero(COP) : new Money(value, COP);
  }
}
