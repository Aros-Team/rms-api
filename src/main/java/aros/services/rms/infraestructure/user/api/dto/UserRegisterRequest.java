/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.dto.CreateUserInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request DTO for user registration. */
public record UserRegisterRequest(
    @NotBlank
        @Pattern(message = "El documento debe contener solo números", regexp = "\\d+")
        @Size(max = 20, message = "El documento debe tener máximo 20 caracteres")
        String document,
    @NotBlank
        @Pattern(
            message = "El nombre solo permite letras y espacios",
            regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")
        @Size(max = 100, message = "El nombre debe tener máximo 100 caracteres")
        String name,
    @NotBlank
        @Email(message = "Ingrese un correo electrónico válido")
        @Size(max = 100, message = "El correo debe tener máximo 100 caracteres")
        String email,
    @Size(max = 200, message = "La dirección debe tener máximo 200 caracteres") String address,
    @NotBlank @Pattern(message = "El teléfono debe tener 10 dígitos", regexp = "\\d{10}")
        String phone) {
  /** Converts this request to CreateUserInfo. */
  public CreateUserInfo toCreateUserInfo() {
    return new CreateUserInfo(document, name, new UserEmail(email), address, phone);
  }
}
