/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/** Response DTO for user data. */
@Schema(description = "Response DTO for user data")
public record UserResponse(
    @Schema(description = "User ID", example = "1") Long id,
    @Schema(description = "Document number", example = "1234567890") String document,
    @Schema(description = "Full name", example = "Jane Doe") String name,
    @Schema(description = "Email address", example = "jane@example.com") String email,
    @Schema(description = "Address", example = "123 Main Street") String address,
    @Schema(description = "Phone number", example = "3001234567") String phone,
    @Schema(description = "User role", example = "ADMIN") UserRole role,
    @Schema(description = "User status", example = "ACTIVE") UserStatus status,
    @Schema(description = "IDs of areas assigned to the user", example = "[1, 2]")
        List<Long> assignedAreas,
    @Schema(description = "Current salary", example = "2500000.00") BigDecimal salary) {
  /** Creates a response from a User domain object. */
  public static UserResponse fromDomain(User user) {
    return new UserResponse(
        user.getId().value(),
        user.getDocument(),
        user.getName(),
        user.getEmail().value(),
        user.getAddress(),
        user.getPhone(),
        user.getRole(),
        user.getStatus(),
        user.getAssignedAreas().stream().map(areaId -> areaId.value()).toList(),
        user.getSalary() != null ? user.getSalary().value() : null);
  }
}
