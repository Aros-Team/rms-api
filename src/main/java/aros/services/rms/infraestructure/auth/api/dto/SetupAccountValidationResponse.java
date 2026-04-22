/* (C) 2026 */
package aros.services.rms.infraestructure.auth.api.dto;

import aros.services.rms.core.user.domain.User;

public record SetupAccountValidationResponse(String name, String email, String role) {

  public static SetupAccountValidationResponse fromDomain(User user) {
    return new SetupAccountValidationResponse(
        user.getName(), user.getEmail().value(), user.getRole().name());
  }
}
