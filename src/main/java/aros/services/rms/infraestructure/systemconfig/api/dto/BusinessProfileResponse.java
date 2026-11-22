/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.BusinessProfile;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response payload for the business profile. */
@Schema(description = "Business identity, contact details and regional settings")
public record BusinessProfileResponse(
    @Schema(description = "Restaurant display name", example = "Rincón del Sabor")
        String restaurantName,
    @Schema(description = "Legal company name", example = "Rincón del Sabor S.A.S.")
        String legalName,
    @Schema(description = "Tax identifier", example = "900123456-7") String taxId,
    @Schema(description = "Physical address", example = "Calle 12 #34-56, Armenia") String address,
    @Schema(description = "Contact phone", example = "+57 606 1234567") String phone,
    @Schema(description = "Contact email", example = "contacto@rincondelsabor.co") String email,
    @Schema(description = "IANA timezone", example = "America/Bogota") String timezone,
    @Schema(description = "BCP 47 locale tag", example = "es-CO") String locale) {

  /**
   * Builds a response from the domain profile.
   *
   * @param profile the domain profile
   * @return the response payload
   */
  public static BusinessProfileResponse fromDomain(BusinessProfile profile) {
    return new BusinessProfileResponse(
        profile.getRestaurantName(),
        profile.getLegalName(),
        profile.getTaxId(),
        profile.getAddress(),
        profile.getPhone(),
        profile.getEmail(),
        profile.getTimezone(),
        profile.getLocale());
  }
}
