/* (C) 2026 */
package aros.services.rms.infraestructure.daymenu.api.dto;

import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Response DTO for day menu history. */
@Schema(description = "Response representing an archived day menu entry")
public record DayMenuHistoryResponse(
    Long id,
    Long productId,
    String productName,
    Double productBasePrice,
    LocalDateTime validFrom,
    LocalDateTime validUntil,
    String createdBy) {

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
