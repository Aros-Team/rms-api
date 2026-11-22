/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.application.exception.InvalidUserAccessException;
import aros.services.rms.core.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Locale;

/** Request payload to change a user's role. */
@Schema(description = "Role change")
public record UserRoleUpdateRequest(
    @Schema(
            description = "New role",
            example = "ADMIN",
            allowableValues = {"ADMIN", "WORKER"})
        @NotBlank(message = "Role is required")
        String role) {

  /**
   * Parses the role value.
   *
   * @return the matching role
   * @throws InvalidUserAccessException when the value is not a supported role
   */
  public UserRole toRole() {
    try {
      return UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new InvalidUserAccessException("Invalid role: " + role);
    }
  }
}
