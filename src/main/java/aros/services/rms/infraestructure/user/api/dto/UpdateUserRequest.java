/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.user.domain.Salary;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

/** Request DTO for updating a user. */
@Schema(description = "Request payload to update an existing user")
public record UpdateUserRequest(
    @Schema(description = "Document number (digits only)", example = "1234567890")
        @NotBlank(message = "Campo obligatorio")
        @Pattern(message = "El documento solo debe contener dígitos", regexp = "\\d+")
        @Size(max = 20, message = "El documento debe tener como máximo 20 caracteres")
        String document,
    @Schema(description = "Full name (letters and spaces only)", example = "Jane Doe")
        @NotBlank(message = "Campo obligatorio")
        @Pattern(
            message = "El nombre solo permite letras y espacios",
            regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")
        @Size(max = 100, message = "El nombre debe tener como máximo 100 caracteres")
        String name,
    @Schema(description = "Email address", example = "jane@example.com")
        @NotBlank(message = "Campo obligatorio")
        @Email(message = "Ingresa un correo electrónico válido")
        @Size(max = 100, message = "El correo debe tener como máximo 100 caracteres")
        String email,
    @Schema(description = "Address (optional, max 200 chars)", example = "123 Main Street")
        @Size(max = 200, message = "La dirección debe tener como máximo 200 caracteres")
        String address,
    @Schema(description = "Phone number (10 digits)", example = "3001234567")
        @NotBlank(message = "Campo obligatorio")
        @Pattern(message = "El teléfono debe tener 10 dígitos", regexp = "\\d{10}")
        String phone,
    @Schema(description = "Job title or role inside the assigned area", example = "Jefe de cocina")
        @Size(max = 100, message = "El rol debe tener como máximo 100 caracteres")
        String jobTitle,
    @Schema(description = "IDs of areas to assign to the user", example = "[1, 2]")
        @NotNull(message = "Las áreas son obligatorias")
        @NotEmpty(message = "Las áreas no pueden estar vacías")
        Set<Long> areas,
    @Schema(
            description = "New monthly salary (optional, triggers history entry)",
            example = "2800000.00")
        BigDecimal salary,
    @Schema(
            description = "Reason for the update (required when salary changes)",
            example = "Annual raise")
        String reason,
    @Schema(description = "Additional observations", example = "Approved by management")
        String observations) {
  /** Converts this request to UpdateUserInfo using the given currency for salary. */
  public UpdateUserInfo toUpdateUserInfo(Currency currency) {
    return new UpdateUserInfo(
        document,
        name,
        new UserEmail(email),
        address,
        phone,
        areas.stream().map(AreaId::of).collect(Collectors.toSet()),
        salary != null ? Salary.of(salary, currency) : null,
        reason,
        observations,
        jobTitle);
  }
}
