/* (C) 2026 */

package aros.services.rms.infraestructure.auth.api.dto;

import aros.services.rms.core.user.domain.User;

/** Response DTO for account setup validation. */
public record SetupAccountValidationResponse(String name, String email, String role) {

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
