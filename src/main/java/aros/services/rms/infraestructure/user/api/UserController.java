/* (C) 2026 */

package aros.services.rms.infraestructure.user.api;

import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.auth.port.input.AccountSetupUseCase;
import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.input.ChangePasswordUseCase;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.input.DeleteUserUseCase;
import aros.services.rms.core.user.port.input.GetAllUsersUseCase;
import aros.services.rms.core.user.port.input.GetSalaryHistoryUseCase;
import aros.services.rms.core.user.port.input.RetryUserEmailUseCase;
import aros.services.rms.core.user.port.input.UpdateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.share.security.JustAccessToken;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.user.api.dto.ChangePasswordRequest;
import aros.services.rms.infraestructure.user.api.dto.SalaryHistoryResponse;
import aros.services.rms.infraestructure.user.api.dto.UpdateUserRequest;
import aros.services.rms.infraestructure.user.api.dto.UserRegisterRequest;
import aros.services.rms.infraestructure.user.api.dto.UserRegisterResponse;
import aros.services.rms.infraestructure.user.api.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller exposing endpoints for user account management. */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/users")
@Slf4j
@Tag(
    name = "Users",
    description = "User management - create, update, delete and manage user accounts")
public class UserController {
  private final CreateUserUseCase createUserUseCase;
  private final ChangePasswordUseCase changePasswordUseCase;
  private final GetAllUsersUseCase getAllUsersUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final RetryUserEmailUseCase retryUserEmailUseCase;
  private final AccountSetupUseCase accountSetupUseCase;
  private final UserRepositoryPort userRepositoryPort;
  private final GetSalaryHistoryUseCase getSalaryHistoryUseCase;

  /**
   * Returns all users in the system. Admin access only.
   *
   * @return list of users
   */
  @GetMapping
  @JustAdminUser
  @Operation(
      summary = "Get all users",
      description = "Returns a list of all users in the system. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<List<UserResponse>> getAll() {
    List<UserResponse> users =
        getAllUsersUseCase.getAll().stream().map(UserResponse::fromDomain).toList();
    return ResponseEntity.ok(users);
  }

  /**
   * Creates a new user and sends a welcome email with temporary credentials. Admin only.
   *
   * @param request user registration payload
   * @return the created user with the temporary raw password
   */
  @PostMapping
  @JustAdminUser
  @Operation(
      summary = "Register new user",
      description =
          "Creates a new user in the system. A welcome email with temporary credentials is sent.",
      responses = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(
            responseCode = "409",
            description = "User already exists with that document or email"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<UserRegisterResponse> register(
      @Valid @RequestBody UserRegisterRequest request) throws UserAlreadyExistsException {
    log.info("Admin is creating a new user: document={}", request.document());
    var result = this.createUserUseCase.create(request.toCreateUserInfo());
    log.info("User created: id={}, status={}", result.user().getId(), result.user().getStatus());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(UserRegisterResponse.fromDomain(result.user(), result.rawPassword()));
  }

  /**
   * Updates an existing user. Admin access only.
   *
   * @param id user ID to update
   * @param request update payload
   * @return the updated user
   */
  @PutMapping("/{id}")
  @JustAdminUser
  @Operation(
      summary = "Update user",
      description = "Updates an existing user's data. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @Parameter(description = "User ID", example = "1")
  public ResponseEntity<UserResponse> update(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request)
      throws UserNotFoundException {
    log.info("Admin updating user: id={}", id);
    var user = this.updateUserUseCase.update(id, request.toUpdateUserInfo());
    log.info("User updated successfully: id={}", id);
    return ResponseEntity.ok(UserResponse.fromDomain(user));
  }

  /**
   * Deletes a user from the system. Admin access only.
   *
   * @param id user ID to delete
   * @return empty response with 204 status
   */
  @DeleteMapping("/{id}")
  @JustAdminUser
  @Operation(
      summary = "Delete user",
      description = "Deletes a user from the system. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @Parameter(description = "User ID", example = "1")
  public ResponseEntity<Void> delete(@PathVariable Long id) throws UserNotFoundException {
    log.info("Admin deleting user: id={}", id);
    this.deleteUserUseCase.delete(id);
    log.info("User deleted successfully: id={}", id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Resends the welcome email to a user. Admin access only.
   *
   * @param id user ID to resend the email to
   * @return empty response indicating success or internal server error
   */
  @PostMapping("/{id}/retry-email")
  @JustAdminUser
  @Operation(
      summary = "Resend registration email",
      description = "Resends the welcome email to a user. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Email resent successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Error sending email")
      })
  @Parameter(description = "User ID", example = "1")
  public ResponseEntity<Void> retryEmail(@PathVariable Long id) throws UserNotFoundException {
    log.info("Admin retrying email for user: id={}", id);
    boolean sent = this.retryUserEmailUseCase.retrySendRegistrationEmail(id);
    if (sent) {
      log.info("Email resent successfully: id={}", id);
      return ResponseEntity.ok().build();
    } else {
      log.warn("Email retry failed: id={}", id);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Invalidates existing setup tokens and sends a new account setup email. Admin only.
   *
   * @param id user ID to resend the setup email to
   * @return empty response on success
   */
  @PostMapping("/{id}/retry-setup-email")
  @JustAdminUser
  @Operation(
      summary = "Resend setup email",
      description =
          "Invalidates existing tokens and sends a new account setup email. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Email resent successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Error sending email")
      })
  @Parameter(description = "User ID", example = "1")
  public ResponseEntity<Void> retrySetupEmail(@PathVariable Long id) throws UserNotFoundException {
    log.info("Admin retrying setup email for user: id={}", id);
    User user =
        userRepositoryPort
            .findById(UserId.of(id))
            .orElseThrow(
                () ->
                    new aros.services.rms.core.user.application.exception.UserNotFoundException(
                        "User not found"));
    accountSetupUseCase.deleteExistingTokens(user.getId());
    accountSetupUseCase.requestSetupEmail(user.getEmail().value());
    log.info("Setup email resent successfully: id={}", id);
    return ResponseEntity.ok().build();
  }

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
      summary = "Change password",
      description = "Allows the authenticated user to change their password.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Current password is incorrect"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
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

  /** Retrieves the salary history for a user. */
  @GetMapping("/{id}/salary-history")
  @JustAdminUser
  public ResponseEntity<List<SalaryHistoryResponse>> getSalaryHistory(@PathVariable Long id) {
    log.info("Admin retrieving salary history for user: id={}", id);
    List<SalaryHistoryResponse> history =
        getSalaryHistoryUseCase.getSalaryHistory(id).stream()
            .map(SalaryHistoryResponse::fromDomain)
            .toList();
    log.info("Salary history retrieved for user: id={}, count={}", id, history.size());
    return ResponseEntity.ok(history);
  }
}
