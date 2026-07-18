/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for the prime cost & margins report — mirrors the A6 contract shape (§5.1). Contains
 * nested static types for period info, series data points, COGS breakdown, labor breakdown, and
 * margins.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Prime cost & margins report")
public class PrimeCostReportResponse {

  @Schema(description = "Queried period info")
  private PeriodResponse period;

  @Schema(description = "Time series data points")
  private List<PrimeCostSeriesResponse> series;

  @Schema(description = "Data completeness flag: FULL | PARTIAL | EMPTY")
  private String dataCompleteness;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Schema(description = "Human-readable notes about data quality")
  private List<String> notes;

  /** Describes the queried time range. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Queried time range")
  public static class PeriodResponse {

    @Schema(description = "Time bucket", example = "monthly")
    private String bucket;

    @Schema(description = "Inclusive start period key", example = "2026-01")
    private String from;

    @Schema(description = "Inclusive end period key", example = "2026-07")
    private String to;

    @Schema(description = "Concrete period keys included in the range")
    private List<String> keys;
  }

  /** One data point in the prime cost time series. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "One data point in the prime cost time series")
  public static class PrimeCostSeriesResponse {

    @Schema(description = "Period key", example = "2026-07")
    private String key;

    @Schema(description = "Net sales amount")
    private MoneyDto netSales;

    @Schema(description = "Gross sales amount")
    private MoneyDto grossSales;

    @Schema(description = "Discounts amount")
    private MoneyDto discounts;

    @Schema(description = "Comp amount")
    private MoneyDto comped;

    @Schema(description = "COGS breakdown")
    private CogsBreakdownResponse cogs;

    @Schema(description = "Labor breakdown")
    private LaborBreakdownResponse labor;

    @Schema(description = "Prime cost (COGS + labor)")
    private MoneyDto primeCost;

    @Schema(description = "Prime cost as percentage of net sales")
    private BigDecimal primeCostPct;

    @Schema(description = "Gross and net profit percentages")
    private MarginsResponse margins;

    @Schema(description = "Data completeness for this period")
    private String dataCompleteness;
  }

  /** COGS broken down by food type category. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "COGS broken down by food type")
  public static class CogsBreakdownResponse {

    @Schema(description = "Total COGS")
    private MoneyDto total;

    @Schema(description = "COGS by category")
    private List<CogsCategoryResponse> byCategory;
  }

  /** One COGS category line with amount and percentage. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "One COGS category line")
  public static class CogsCategoryResponse {

    @Schema(description = "Category name", example = "FOOD")
    private String category;

    @Schema(description = "Amount for this category")
    private MoneyDto amount;

    @Schema(description = "Percentage of total COGS")
    private BigDecimal pct;
  }

  /** Labor broken down by area group (FOH, BOH). */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Labor broken down by area group")
  public static class LaborBreakdownResponse {

    @Schema(description = "Total labor cost")
    private MoneyDto total;

    @Schema(description = "Labor by area")
    private List<LaborAreaResponse> byArea;
  }

  /** One labor area line with amount and percentage. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "One labor area line")
  public static class LaborAreaResponse {

    @Schema(description = "Area group", example = "FOH")
    private String area;

    @Schema(description = "Amount for this area")
    private MoneyDto amount;

    @Schema(description = "Percentage of total labor")
    private BigDecimal pct;
  }

  /** Gross and net profit percentages. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Gross and net profit percentages")
  public static class MarginsResponse {

    @Schema(description = "Gross profit percentage")
    private BigDecimal grossProfitPct;

    @Schema(description = "Net profit percentage")
    private BigDecimal netProfitPct;
  }
}
