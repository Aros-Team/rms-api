/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.dto;

import aros.services.rms.infraestructure.common.dto.MoneyDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for the operations (RevPASH, covers and turns) report. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Operational efficiency report")
public class OperationsReportResponse {

  @Schema(description = "Queried period info")
  private PeriodResponse period;

  @Schema(description = "Revenue per available seat hour")
  private RevPashResponse revPash;

  @Schema(description = "Table turnover overall and per day part")
  private TableTurnoverResponse tableTurnover;

  @Schema(description = "Labor cost and share of net sales")
  private LaborResponse labor;

  @Schema(description = "Total covers in the range", example = "46229")
  private long covers;

  @Schema(description = "Total delivered orders in the range", example = "17135")
  private long orders;

  @Schema(description = "Average occupied minutes per order", example = "60.50")
  private BigDecimal avgOccupancyMinutes;

  @Schema(description = "Occupied seat minutes (debug value)", example = "2801527")
  private long totalOccupiedSeatMinutes;

  @Schema(description = "Data completeness flag: FULL | PARTIAL | EMPTY")
  private String dataCompleteness;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Schema(description = "Human-readable notes about data quality")
  private List<String> notes;

  /** Queried time range. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Queried time range")
  public static class PeriodResponse {
    private String bucket;
    private String from;
    private String to;
    private List<String> keys;
  }

  /** Revenue per available seat hour. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Revenue per available seat hour")
  public static class RevPashResponse {
    private MoneyDto value;
    private MoneyDto totalNetSales;
    private BigDecimal totalAvailableSeatHours;
  }

  /** Table turnover. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Table turnover")
  public static class TableTurnoverResponse {
    private BigDecimal overall;
    private List<DayPartTurnoverResponse> byDayPart;
  }

  /** Table turnover for one day part. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Table turnover for one day part")
  public static class DayPartTurnoverResponse {
    private String dayPart;
    private BigDecimal turns;
    private long covers;
    private long tables;
  }

  /** Labor cost and share. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Labor cost and share of net sales")
  public static class LaborResponse {
    private MoneyDto totalCost;
    private BigDecimal sharePct;
  }
}
