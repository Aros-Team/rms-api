/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api.dto;

import aros.services.rms.core.payroll.domain.PayrollEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Response DTO for payroll event data. */
@Schema(description = "Response DTO for payroll event data")
public record PayrollEventResponse(
    @Schema(description = "Event ID", example = "1") Long id,
    @Schema(description = "User ID", example = "1") Long userId,
    @Schema(description = "Event date", example = "2026-08-01") LocalDate eventDate,
    @Schema(description = "Event type", example = "OVERTIME") String eventType,
    @Schema(description = "Quantity", example = "2.5") BigDecimal quantity,
    @Schema(description = "Unit rate", example = "25000") BigDecimal unitRate,
    @Schema(description = "Total amount", example = "62500") BigDecimal amount,
    @Schema(description = "Optional notes") String notes,
    @Schema(description = "Creation timestamp", example = "2026-08-01T10:30:00Z") Instant createdAt,
    @Schema(description = "Created by") String createdBy) {

  /** Creates a PayrollEventResponse from a PayrollEvent domain object. */
  public static PayrollEventResponse fromDomain(PayrollEvent event) {
    if (event == null) {
      return null;
    }
    return new PayrollEventResponse(
        event.id(),
        event.userId(),
        event.eventDate(),
        event.eventType() != null ? event.eventType().name() : null,
        event.quantity(),
        event.unitRate(),
        event.amount(),
        event.notes(),
        event.createdAt(),
        event.createdBy());
  }
}
