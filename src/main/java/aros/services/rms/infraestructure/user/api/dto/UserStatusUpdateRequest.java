/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.application.exception.InvalidUserAccessException;
import aros.services.rms.core.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Locale;

/** Request payload to activate or deactivate a user. */
@Schema(description = "Status change")
public record UserStatusUpdateRequest(
    @Schema(
            description = "New status",
            example = "INACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE"})
        @NotBlank(message = "El estado es obligatorio")
        String status) {

  /**
   * Parses the status value.
   *
   * @return the matching status
   * @throws InvalidUserAccessException when the value is not a supported status
   */
  public UserStatus toStatus() {
    try {
      return UserStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new InvalidUserAccessException("Estado inválido: " + status);
    }
  }
}
