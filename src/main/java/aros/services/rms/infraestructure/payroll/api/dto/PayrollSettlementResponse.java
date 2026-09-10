/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api.dto;

import aros.services.rms.core.payroll.domain.PayrollSettlement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Response DTO for payroll settlement data. */
@Schema(description = "Response DTO for payroll settlement data")
public record PayrollSettlementResponse(
    @Schema(description = "Settlement ID", example = "1") Long id,
    @Schema(description = "Payroll ID", example = "1") Long payrollId,
    @Schema(description = "User ID", example = "1") Long userId,
    @Schema(description = "Settlement type", example = "MONTHLY") String settlementType,
    @Schema(description = "Period start", example = "2026-08-01") LocalDate periodStart,
    @Schema(description = "Period end", example = "2026-08-31") LocalDate periodEnd,
    @Schema(description = "Amount", example = "2500000") BigDecimal amount,
    @Schema(description = "Optional notes") String notes,
    @Schema(description = "Settlement timestamp", example = "2026-08-31T10:30:00Z")
        Instant settledAt,
    @Schema(description = "Settled by") String settledBy) {

  /** Creates a PayrollSettlementResponse from a PayrollSettlement domain object. */
  public static PayrollSettlementResponse fromDomain(PayrollSettlement settlement) {
    if (settlement == null) {
      return null;
    }
    return new PayrollSettlementResponse(
        settlement.id(),
        settlement.payrollId(),
        settlement.userId(),
        settlement.settlementType() != null ? settlement.settlementType().name() : null,
        settlement.periodStart(),
        settlement.periodEnd(),
        settlement.amount(),
        settlement.notes(),
        settlement.settledAt(),
        settlement.settledBy());
  }
}
