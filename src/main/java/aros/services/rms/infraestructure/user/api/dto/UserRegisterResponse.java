/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for user registration. */
@Schema(
    description = "Response DTO for a newly registered user, including the temporary raw password")
public record UserRegisterResponse(
    @Schema(description = "User ID", example = "42") Long id,
    @Schema(description = "Document number", example = "1234567890") String document,
    @Schema(description = "Full name", example = "Jane Doe") String name,
    @Schema(description = "Email address", example = "jane@example.com") String email,
    @Schema(
            description = "Temporary raw password (only returned at creation)",
            example = "Tmp@12345")
        String password) {
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
