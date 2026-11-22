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

/** Response DTO for the customer cohort report. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer cohort report")
public class CohortReportResponse {

  @Schema(description = "Queried period info")
  private PeriodResponse period;

  @Schema(description = "Orders whose fingerprint was not seen in the window", example = "18")
  private int newClients;

  @Schema(description = "Orders whose fingerprint was seen in the window", example = "12")
  private int recurringClients;

  @Schema(description = "Total orders in the range", example = "30")
  private int totalOrders;

  @Schema(description = "Recurring rate percentage", example = "40.00")
  private BigDecimal recurringRatePct;

  @Schema(description = "Average ticket per order")
  private MoneyDto averageTicketPerOrder;

  @Schema(description = "Average ticket per cover")
  private MoneyDto averageTicketPerCover;

  @Schema(description = "Fingerprint strategy: PHONE | PHONE_OR_TABLE | TABLE_ONLY")
  private String fingerprintStrategy;

  @Schema(description = "Look-back window in days", example = "90")
  private int cohortWindowDays;

  @Schema(description = "One row per bucket key")
  private List<CohortWindowResponse> cohorts;

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

  /** One bucket key inside the range. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "One cohort window")
  public static class CohortWindowResponse {
    private String key;
    private int newClients;
    private int recurringClients;
    private int totalOrders;
    private BigDecimal recurringRatePct;
  }
}
