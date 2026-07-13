/* (C) 2026 */

package aros.services.rms.infraestructure.daymenu.api.dto;

import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Response DTO for day menu history. */
@Schema(description = "Response representing an archived day menu entry")
public record DayMenuHistoryResponse(
    @Schema(description = "History entry ID", example = "10") Long id,
    @Schema(description = "Product ID of the archived day menu", example = "3") Long productId,
    @Schema(description = "Product name", example = "Pasta Carbonara") String productName,
    @Schema(description = "Product base price", example = "18500.00") Double productBasePrice,
    @Schema(
            description = "Timestamp from which the menu was valid",
            example = "2026-07-01T00:00:00")
        LocalDateTime validFrom,
    @Schema(
            description = "Timestamp until which the menu was valid",
            example = "2026-07-12T23:59:59")
        LocalDateTime validUntil,
    @Schema(description = "Username that set the day menu", example = "admin") String createdBy) {

  /**
   * Creates a response from a domain object.
   *
   * @param domain the day menu history domain
   * @return the response DTO
   */
  public static DayMenuHistoryResponse fromDomain(DayMenuHistory domain) {
    return new DayMenuHistoryResponse(
        domain.getId(),
        domain.getProductId(),
        domain.getProductName(),
        domain.getProductBasePrice(),
        domain.getValidFrom(),
        domain.getValidUntil(),
        domain.getCreatedBy());
  }
}
