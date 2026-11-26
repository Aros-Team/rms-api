/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.user.domain.Salary;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.dto.CreateUserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

/** Request DTO for user registration. */
@Schema(description = "Request payload to register a new user")
public record UserRegisterRequest(
    @Schema(description = "Document number (digits only)", example = "1234567890")
        @NotBlank
        @Pattern(message = "El documento solo debe contener dígitos", regexp = "\\d+")
        @Size(max = 20, message = "El documento debe tener como máximo 20 caracteres")
        String document,
    @Schema(description = "Full name (letters and spaces only)", example = "Jane Doe")
        @NotBlank
        @Pattern(
            message = "El nombre solo permite letras y espacios",
            regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")
        @Size(max = 100, message = "El nombre debe tener como máximo 100 caracteres")
        String name,
    @Schema(description = "Email address", example = "jane@example.com")
        @NotBlank
        @Email(message = "Ingresa un correo electrónico válido")
        @Size(max = 100, message = "El correo debe tener como máximo 100 caracteres")
        String email,
    @Schema(description = "Address (optional, max 200 chars)", example = "123 Main Street")
        @Size(max = 200, message = "La dirección debe tener como máximo 200 caracteres")
        String address,
    @Schema(description = "Phone number (10 digits)", example = "3001234567")
        @NotBlank
        @Pattern(message = "El teléfono debe tener 10 dígitos", regexp = "\\d{10}")
        String phone,
    @Schema(description = "IDs of areas to assign to the user", example = "[1, 2]")
        @NotNull(message = "Las áreas son obligatorias")
        @NotEmpty(message = "Las áreas no pueden estar vacías")
        Set<Long> areas,
    @Schema(description = "Monthly salary (must be positive)", example = "2500000.00")
        @Positive(message = "El salario debe ser un valor positivo")
        BigDecimal salary) {
  /** Converts this request to CreateUserInfo using the given currency for salary. */
  public CreateUserInfo toCreateUserInfo(Currency currency) {
    return new CreateUserInfo(
        document,
        name,
        new UserEmail(email),
        address,
        phone,
        areas.stream().map(AreaId::of).collect(Collectors.toSet()),
        salary != null ? Salary.of(salary, currency) : null);
  }
}
