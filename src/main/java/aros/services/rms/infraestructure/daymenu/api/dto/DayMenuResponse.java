/* (C) 2026 */

package aros.services.rms.infraestructure.daymenu.api.dto;

import aros.services.rms.core.daymenu.domain.DayMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Response DTO for day menu. */
@Schema(description = "Response representing the active day menu")
public record DayMenuResponse(
    @Schema(description = "Day menu ID", example = "1") Long id,
    @Schema(description = "Product ID of the day menu", example = "5") Long productId,
    @Schema(description = "Product name", example = "Burger of the Day") String productName,
    @Schema(description = "Product base price", example = "15900.00") Double productBasePrice,
    @Schema(description = "Timestamp from which the menu is valid", example = "2026-07-13T00:00:00")
        LocalDateTime validFrom,
    @Schema(description = "Username that set the day menu", example = "admin") String createdBy) {

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
