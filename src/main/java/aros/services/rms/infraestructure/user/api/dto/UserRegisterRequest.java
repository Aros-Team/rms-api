package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.dto.CreateUserInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Request DTO for user registration. */
public record UserRegisterRequest(
    @NotBlank @Pattern(message = "El documento debe contener solo números", regexp = "\\d+") String document,
    @NotBlank String name,
    @NotBlank @Email String email,
    String address,
    @NotBlank @Pattern(message = "El teléfono debe tener 10 dígitos", regexp = "\\d{10}") String phone) {
  /** Converts this request to CreateUserInfo. */
  public CreateUserInfo toCreateUserInfo() {
    return new CreateUserInfo(document, name, new UserEmail(email), address, phone);
  }
}
