/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.BusinessProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request payload for the business profile. */
@Schema(description = "Business identity, contact details and regional settings")
public record BusinessProfileRequest(
    @Schema(description = "Restaurant display name", example = "Rincón del Sabor")
        @NotBlank(message = "Restaurant name is required")
        @Size(max = 120, message = "Restaurant name must not exceed 120 characters")
        String restaurantName,
    @Schema(description = "Legal company name", example = "Rincón del Sabor S.A.S.")
        @Size(max = 160, message = "Legal name must not exceed 160 characters")
        String legalName,
    @Schema(description = "Tax identifier", example = "900123456-7")
        @Size(max = 40, message = "Tax id must not exceed 40 characters")
        String taxId,
    @Schema(description = "Physical address", example = "Calle 12 #34-56, Armenia")
        @Size(max = 200, message = "Address must not exceed 200 characters")
        String address,
    @Schema(description = "Contact phone", example = "+57 606 1234567")
        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,
    @Schema(description = "Contact email", example = "contacto@rincondelsabor.co")
        @Email(message = "Enter a valid email address")
        @Size(max = 120, message = "Email must not exceed 120 characters")
        String email,
    @Schema(description = "IANA timezone", example = "America/Bogota")
        @NotBlank(message = "Timezone is required")
        @Size(max = 60, message = "Timezone must not exceed 60 characters")
        String timezone,
    @Schema(description = "BCP 47 locale tag", example = "es-CO")
        @NotBlank(message = "Locale is required")
        @Size(max = 20, message = "Locale must not exceed 20 characters")
        String locale) {

  /**
   * Converts the request to the domain profile.
   *
   * @return the domain profile
   */
  public BusinessProfile toDomain() {
    return BusinessProfile.builder()
        .restaurantName(restaurantName)
        .legalName(legalName)
        .taxId(taxId)
        .address(address)
        .phone(phone)
        .email(email)
        .timezone(timezone)
        .locale(locale)
        .build();
  }
}
