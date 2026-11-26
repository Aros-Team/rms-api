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
        @NotBlank(message = "El nombre del restaurante es obligatorio")
        @Size(max = 120, message = "El nombre del restaurante no debe superar los 120 caracteres")
        String restaurantName,
    @Schema(description = "Legal company name", example = "Rincón del Sabor S.A.S.")
        @Size(max = 160, message = "La razón social no debe superar los 160 caracteres")
        String legalName,
    @Schema(description = "Tax identifier", example = "900123456-7")
        @Size(max = 40, message = "El NIT no debe superar los 40 caracteres")
        String taxId,
    @Schema(description = "Physical address", example = "Calle 12 #34-56, Armenia")
        @Size(max = 200, message = "La dirección no debe superar los 200 caracteres")
        String address,
    @Schema(description = "Contact phone", example = "+57 606 1234567")
        @Size(max = 30, message = "El teléfono no debe superar los 30 caracteres")
        String phone,
    @Schema(description = "Contact email", example = "contacto@rincondelsabor.co")
        @Email(message = "Ingresa un correo electrónico válido")
        @Size(max = 120, message = "El correo no debe superar los 120 caracteres")
        String email,
    @Schema(description = "IANA timezone", example = "America/Bogota")
        @NotBlank(message = "La zona horaria es obligatoria")
        @Size(max = 60, message = "La zona horaria no debe superar los 60 caracteres")
        String timezone,
    @Schema(description = "BCP 47 locale tag", example = "es-CO")
        @NotBlank(message = "La configuración regional es obligatoria")
        @Size(max = 20, message = "La configuración regional no debe superar los 20 caracteres")
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
