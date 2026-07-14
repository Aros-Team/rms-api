/* (C) 2026 */

package aros.services.rms.infraestructure.user.api;

import aros.services.rms.core.user.port.input.ChangePasswordUseCase;
import aros.services.rms.infraestructure.share.security.JustAccessToken;
import aros.services.rms.infraestructure.user.api.dto.ChangePasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for self-service user operations. */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/users")
@Slf4j
@Tag(name = "Users", description = "Self-service operations for authenticated users")
public class UserController {

  private final ChangePasswordUseCase changePasswordUseCase;

  /**
   * Allows the authenticated user to change their password.
   *
   * @param request password change payload
   * @param jwt authenticated principal's JWT
   * @return empty response on success
   */
  @PutMapping("/me/password")
  @JustAccessToken
  @Operation(
      tags = {"Users"},
      summary = "Change password",
      description = "Allows the authenticated user to change their password.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Current password is incorrect"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal Jwt jwt) {
    String email = jwt.getSubject();
    log.info("User changing password: email={}", email);
    changePasswordUseCase.changePassword(email, request.currentPassword(), request.newPassword());
    log.info("Password changed successfully: email={}", email);
    return ResponseEntity.ok().build();
  }
}
