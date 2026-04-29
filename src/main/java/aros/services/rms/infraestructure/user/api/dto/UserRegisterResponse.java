/* (C) 2026 */
package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.User;

/** Response DTO for user registration. */
public record UserRegisterResponse(
    Long id, String document, String name, String email, String password) {
  /** Creates a response from a User domain object. */
  public static UserRegisterResponse fromDomain(User user, String rawPassword) {
    return new UserRegisterResponse(
        user.getId().value(),
        user.getDocument(),
        user.getName(),
        user.getEmail().value(),
        rawPassword);
  }
}
