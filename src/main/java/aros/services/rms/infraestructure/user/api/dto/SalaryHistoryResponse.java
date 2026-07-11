/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import java.math.BigDecimal;
import java.time.Instant;

/** Response DTO for salary history entries. */
public record SalaryHistoryResponse(
    BigDecimal oldSalary,
    BigDecimal newSalary,
    Instant changedAt,
    String reason,
    String observations) {

  /**
   * Creates a response from a domain entry.
   *
   * @param entry the domain entry
   * @return the response DTO
   */
  public static SalaryHistoryResponse fromDomain(SalaryHistoryEntry entry) {
    return new SalaryHistoryResponse(
        entry.getOldSalary() != null ? entry.getOldSalary().value() : null,
        entry.getNewSalary() != null ? entry.getNewSalary().value() : null,
        entry.getChangedAt(),
        entry.getReason(),
        entry.getObservations());
  }
}
