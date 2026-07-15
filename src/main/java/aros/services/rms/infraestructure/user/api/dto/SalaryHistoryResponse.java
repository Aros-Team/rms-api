/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

/** Response DTO for salary history entries. */
@Schema(description = "Response DTO for salary change history entries")
public record SalaryHistoryResponse(
    @Schema(description = "Previous salary (null for first entry)", example = "2000000.00")
        BigDecimal oldSalary,
    @Schema(description = "New salary after the change", example = "2500000.00")
        BigDecimal newSalary,
    @Schema(description = "Timestamp when the change was applied", example = "2026-03-08T14:30:00Z")
        Instant changedAt,
    @Schema(description = "Reason for the salary change", example = "Annual raise") String reason,
    @Schema(description = "Additional observations", example = "Approved by management")
        String observations) {

  /**
   * Creates a response from a domain entry.
   *
   * @param entry the domain entry
   * @return the response DTO
   */
  public static SalaryHistoryResponse fromDomain(SalaryHistoryEntry entry) {
    return new SalaryHistoryResponse(
        entry.getOldSalary() != null ? entry.getOldSalary().value().amount() : null,
        entry.getNewSalary() != null ? entry.getNewSalary().value().amount() : null,
        entry.getChangedAt(),
        entry.getReason(),
        entry.getObservations());
  }
}
