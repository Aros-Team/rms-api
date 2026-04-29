/* (C) 2026 */
package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request DTO for updating a user. */
public record UpdateUserRequest(
    @NotBlank(message = "Document is required") String document,
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
    @NotBlank(message = "Address is required") String address,
    @NotBlank(message = "Phone is required") @Size(min = 10, max = 20, message = "Phone must be between 10 and 20 characters") String phone) {
  /** Converts this request to UpdateUserInfo. */
  public UpdateUserInfo toUpdateUserInfo() {
    return new UpdateUserInfo(document, name, new UserEmail(email), address, phone);
  }
}
