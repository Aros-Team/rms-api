/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Request DTO for updating a user. */
public record UpdateUserRequest(
    @NotBlank(message = "Campo requerido") String document,
    @NotBlank(message = "Campo requerido") String name,
    @NotBlank(message = "Campo requerido") @Email(message = "Ingrese un correo electrónico válido") String email,
    @NotBlank(message = "Campo requerido") String address,
    @NotBlank(message = "Campo requerido") @Pattern(message = "El teléfono debe tener 10 dígitos", regexp = "\\d{10}") String phone) {
  /** Converts this request to UpdateUserInfo. */
  public UpdateUserInfo toUpdateUserInfo() {
    return new UpdateUserInfo(document, name, new UserEmail(email), address, phone);
  }
}
