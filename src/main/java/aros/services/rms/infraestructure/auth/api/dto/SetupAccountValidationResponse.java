/* (C) 2026 */

package aros.services.rms.infraestructure.auth.api.dto;

import aros.services.rms.core.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for account setup validation. */
@Schema(description = "Response DTO returned when validating a setup token")
public record SetupAccountValidationResponse(
    @Schema(description = "User full name", example = "Jane Doe") String name,
    @Schema(description = "User email", example = "jane@example.com") String email,
    @Schema(description = "User role (ADMIN, WORKER, etc.)", example = "ADMIN") String role) {

  /**
   * Creates a response from a user domain object.
   *
   * @param user the user domain object
   * @return the response DTO
   */
  public static SetupAccountValidationResponse fromDomain(User user) {
    return new SetupAccountValidationResponse(
        user.getName(), user.getEmail().value(), user.getRole().name());
  }
}
