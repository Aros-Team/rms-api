/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.api.dto;

import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Response DTO for the current or newly opened shift. */
@Schema(description = "Cash shift header")
public record CashShiftResponse(
    @Schema(description = "Shift ID") Long id,
    @Schema(description = "Status", example = "OPEN") String status,
    @Schema(description = "Currency", example = "COP") String currency,
    @Schema(description = "User who opened the shift") Long openedBy,
    @Schema(description = "Open timestamp") LocalDateTime openedAt,
    @Schema(description = "Opening float") MoneyDto openingFloat,
    @Schema(description = "Close notes") String notes) {}
