/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
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
  @Builder.Default private Money netSales = null;
  @Builder.Default private Money grossSales = null;
  @Builder.Default private Money discounts = null;
  @Builder.Default private Money comped = null;
  @Builder.Default private Money cogsFood = null;
  @Builder.Default private Money cogsBeverage = null;
  @Builder.Default private Money cogsAlcohol = null;
  @Builder.Default private Money cogsOther = null;
  @Builder.Default private BigDecimal foodCogsPct = BigDecimal.ZERO;
  @Builder.Default private Money laborFoh = null;
  @Builder.Default private Money laborBoh = null;
  @Builder.Default private Money laborTotal = null;
  @Builder.Default private BigDecimal laborPct = BigDecimal.ZERO;
  @Builder.Default private Money primeCost = null;
  @Builder.Default private BigDecimal primeCostPct = BigDecimal.ZERO;
  @Builder.Default private BigDecimal grossProfitPct = BigDecimal.ZERO;
  @Builder.Default private BigDecimal netProfitPct = BigDecimal.ZERO;
  @Builder.Default private String dataCompleteness = "FULL";
}
