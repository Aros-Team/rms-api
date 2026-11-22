/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response DTO for the settings user list; never exposes password material. */
@Schema(description = "User account summary for settings")
public record UserSummaryResponse(
    @Schema(description = "User ID", example = "1001") Long id,
    @Schema(description = "Document number", example = "1234567890") String document,
    @Schema(description = "Full name", example = "Jane Doe") String name,
    @Schema(description = "Email address", example = "jane@example.com") String email,
    @Schema(description = "Phone number", example = "3001234567") String phone,
    @Schema(description = "User role", example = "WORKER") UserRole role,
    @Schema(description = "User status", example = "ACTIVE") UserStatus status,
    @Schema(description = "Assigned area ids", example = "[2]") List<Long> areaIds) {

  /**
   * Builds a summary from the domain user.
   *
   * @param user the domain user
   * @return the summary payload
   */
  public static UserSummaryResponse fromDomain(User user) {
    List<Long> areaIds =
        user.getAssignedAreas() == null
            ? List.of()
            : user.getAssignedAreas().stream().map(areaId -> areaId.value()).toList();
    return new UserSummaryResponse(
        user.getId().value(),
        user.getDocument(),
        user.getName(),
        user.getEmail().value(),
        user.getPhone(),
        user.getRole(),
        user.getStatus(),
        areaIds);
  }
}
