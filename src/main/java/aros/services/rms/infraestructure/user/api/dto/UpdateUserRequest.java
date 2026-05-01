/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request DTO for updating a user. */
public record UpdateUserRequest(
    @NotBlank(message = "Campo requerido") @Pattern(message = "El documento debe contener solo números", regexp = "\\d+") @Size(max = 20, message = "El documento debe tener máximo 20 caracteres") String document,
    @NotBlank(message = "Campo requerido") @Pattern(message = "El nombre solo puede contener letras y espacios", regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$") @Size(max = 100, message = "El nombre debe tener máximo 100 caracteres") String name,
    @NotBlank(message = "Campo requerido") @Email(message = "Ingrese un correo electrónico válido") @Size(max = 100, message = "El correo debe tener máximo 100 caracteres") String email,
    @NotBlank(message = "Campo requerido") @Size(max = 200, message = "La dirección debe tener máximo 200 caracteres") String address,
    @NotBlank(message = "Campo requerido") @Pattern(message = "El teléfono debe tener 10 dígitos", regexp = "\\d{10}") String phone) {
  /** Converts this request to UpdateUserInfo. */
  public UpdateUserInfo toUpdateUserInfo() {
    return new UpdateUserInfo(document, name, new UserEmail(email), address, phone);
  }
}
