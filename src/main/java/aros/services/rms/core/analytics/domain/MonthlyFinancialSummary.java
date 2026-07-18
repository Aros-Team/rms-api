/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain record for a monthly financial summary row. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFinancialSummary {

  private Long id;
  private String periodKey;
  private String bucket;
  @Builder.Default private Money netSales = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private Money grossSales = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private Money discounts = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private Money comped = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private Money cogsFood = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private Money cogsBeverage = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private Money cogsAlcohol = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private Money cogsOther = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private BigDecimal foodCogsPct = BigDecimal.ZERO;
  @Builder.Default private Money laborFoh = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private Money laborBoh = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private Money laborTotal = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private BigDecimal laborPct = BigDecimal.ZERO;
  @Builder.Default private Money primeCost = Money.zero(Currency.getInstance("COP"));
  @Builder.Default private BigDecimal primeCostPct = BigDecimal.ZERO;
  @Builder.Default private BigDecimal grossProfitPct = BigDecimal.ZERO;
  @Builder.Default private BigDecimal netProfitPct = BigDecimal.ZERO;
  @Builder.Default private String dataCompleteness = "FULL";
}
