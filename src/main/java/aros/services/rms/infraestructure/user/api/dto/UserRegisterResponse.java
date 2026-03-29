/* (C) 2026 */
package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.User;

public record UserRegisterResponse(
    Long id, String document, String name, String email, String password) {

  public static UserRegisterResponse fromDomain(User user, String rawPassword) {
    return new UserRegisterResponse(
        user.getId().value(),
        user.getDocument(),
        user.getName(),
        user.getEmail().value(),
        rawPassword);
  }
}
