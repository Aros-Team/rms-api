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
import java.util.Set;
import java.util.stream.Collectors;

/** Request DTO for user registration. */
@Schema(description = "Request payload to register a new user")
public record UserRegisterRequest(
    @Schema(description = "Document number (digits only)", example = "1234567890")
        @NotBlank
        @Pattern(message = "Document must contain only digits", regexp = "\\d+")
        @Size(max = 20, message = "Document must have at most 20 characters")
        String document,
    @Schema(description = "Full name (letters and spaces only)", example = "Jane Doe")
        @NotBlank
        @Pattern(
            message = "Name allows only letters and spaces",
            regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")
        @Size(max = 100, message = "Name must have at most 100 characters")
        String name,
    @Schema(description = "Email address", example = "jane@example.com")
        @NotBlank
        @Email(message = "Enter a valid email address")
        @Size(max = 100, message = "Email must have at most 100 characters")
        String email,
    @Schema(description = "Address (optional, max 200 chars)", example = "123 Main Street")
        @Size(max = 200, message = "Address must have at most 200 characters")
        String address,
    @Schema(description = "Phone number (10 digits)", example = "3001234567")
        @NotBlank
        @Pattern(message = "Phone must have 10 digits", regexp = "\\d{10}")
        String phone,
    @Schema(description = "IDs of areas to assign to the user", example = "[1, 2]")
        @NotNull(message = "Areas are required")
        @NotEmpty(message = "Areas cannot be empty")
        Set<Long> areas,
    @Schema(description = "Monthly salary (must be positive)", example = "2500000.00")
        @Positive(message = "Salary must be a positive value")
        BigDecimal salary) {
  /** Converts this request to CreateUserInfo. */
  public CreateUserInfo toCreateUserInfo() {
    return new CreateUserInfo(
        document,
        name,
        new UserEmail(email),
        address,
        phone,
        areas.stream().map(AreaId::of).collect(Collectors.toSet()),
        salary != null ? Salary.of(salary) : null);
  }
}
