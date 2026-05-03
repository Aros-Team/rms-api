/* (C) 2026 */

package aros.services.rms.infraestructure.daymenu.api.dto;

import aros.services.rms.core.daymenu.domain.DayMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Response DTO for day menu. */
@Schema(description = "Response representing the active day menu")
public record DayMenuResponse(
    Long id,
    Long productId,
    String productName,
    Double productBasePrice,
    LocalDateTime validFrom,
    String createdBy) {

  /**
   * Creates a response from a domain object.
   *
   * @param domain the day menu domain
   * @return the response DTO
   */
  public static DayMenuResponse fromDomain(DayMenu domain) {
    return new DayMenuResponse(
        domain.getId(),
        domain.getProductId(),
        domain.getProductName(),
        domain.getProductBasePrice(),
        domain.getValidFrom(),
        domain.getCreatedBy());
  }
}
