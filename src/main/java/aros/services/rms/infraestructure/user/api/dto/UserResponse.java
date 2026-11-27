/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Response DTO for user data. */
@Schema(description = "Response DTO for user data")
public record UserResponse(
    @Schema(description = "User ID", example = "1") Long id,
    @Schema(description = "Document number", example = "1234567890") String document,
    @Schema(description = "Full name", example = "Jane Doe") String name,
    @Schema(description = "Email address", example = "jane@example.com") String email,
    @Schema(description = "Address", example = "123 Main Street") String address,
    @Schema(description = "Phone number", example = "3001234567") String phone,
    @Schema(description = "Job title or role inside the assigned area", example = "Jefe de cocina")
        String jobTitle,
    @Schema(description = "User role", example = "ADMIN") UserRole role,
    @Schema(description = "User status", example = "ACTIVE") UserStatus status,
    @Schema(description = "IDs of areas assigned to the user", example = "[1, 2]")
        List<Long> assignedAreas,
    @Schema(description = "Current salary") MoneyDto salary,
    @Schema(
            description = "Profile image URL (tablet variant)",
            example = "http://localhost:8080/api/v1/images/local/users/12/seed/tablet.webp")
        String imageUrl,
    @Schema(description = "Activities assigned per area")
        List<AreaActivityAssignmentResponse> areaActivities) {
  /** Creates a response from a User domain object. */
  public static UserResponse fromDomain(User user) {
    return fromDomain(user, null, Map.of());
  }

  /** Creates a response from a User domain object plus its profile image URL. */
  public static UserResponse fromDomain(User user, String imageUrl) {
    return fromDomain(user, imageUrl, Map.of());
  }

  /** Creates a response with the worker's explicit activities per area. */
  public static UserResponse fromDomain(
      User user,
      String imageUrl,
      Map<Long, Set<aros.services.rms.core.area.domain.AreaActivity>> activitiesByArea) {
    return new UserResponse(
        user.getId().value(),
        user.getDocument(),
        user.getName(),
        user.getEmail().value(),
        user.getAddress(),
        user.getPhone(),
        user.getJobTitle(),
        user.getRole(),
        user.getStatus(),
        user.getAssignedAreas().stream().map(areaId -> areaId.value()).toList(),
        user.getSalary() != null ? MoneyDto.of(user.getSalary().value()) : null,
        imageUrl,
        activitiesByArea == null
            ? List.of()
            : activitiesByArea.entrySet().stream()
                .map(entry -> AreaActivityAssignmentResponse.of(entry.getKey(), entry.getValue()))
                .toList());
  }
}
