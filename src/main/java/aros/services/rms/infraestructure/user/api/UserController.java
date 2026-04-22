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
import aros.services.rms.core.user.port.input.RetryUserEmailUseCase;
import aros.services.rms.core.user.port.input.UpdateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.share.security.JustAccessToken;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.user.api.dto.ChangePasswordRequest;
import aros.services.rms.infraestructure.user.api.dto.UpdateUserRequest;
import aros.services.rms.infraestructure.user.api.dto.UserRegisterRequest;
import aros.services.rms.infraestructure.user.api.dto.UserRegisterResponse;
import aros.services.rms.infraestructure.user.api.dto.UserResponse;
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

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/users")
@Slf4j
public class UserController {
  private final CreateUserUseCase createUserUseCase;
  private final ChangePasswordUseCase changePasswordUseCase;
  private final GetAllUsersUseCase getAllUsersUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final RetryUserEmailUseCase retryUserEmailUseCase;
  private final AccountSetupUseCase accountSetupUseCase;
  private final UserRepositoryPort userRepositoryPort;

  @GetMapping
  @JustAdminUser
  public ResponseEntity<List<UserResponse>> getAll() {
    List<UserResponse> users =
        getAllUsersUseCase.getAll().stream().map(UserResponse::fromDomain).toList();
    return ResponseEntity.ok(users);
  }

  @PostMapping
  @JustAdminUser
  public ResponseEntity<UserRegisterResponse> register(
      @Valid @RequestBody UserRegisterRequest request) throws UserAlreadyExistsException {
    log.info("Admin is creating a new user: document={}", request.document());
    var result = this.createUserUseCase.create(request.toCreateUserInfo());
    log.info("User created: id={}, status={}", result.user().getId(), result.user().getStatus());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(UserRegisterResponse.fromDomain(result.user(), result.rawPassword()));
  }

  @PutMapping("/{id}")
  @JustAdminUser
  public ResponseEntity<UserResponse> update(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request)
      throws UserNotFoundException {
    log.info("Admin updating user: id={}", id);
    var user = this.updateUserUseCase.update(id, request.toUpdateUserInfo());
    log.info("User updated successfully: id={}", id);
    return ResponseEntity.ok(UserResponse.fromDomain(user));
  }

  @DeleteMapping("/{id}")
  @JustAdminUser
  public ResponseEntity<Void> delete(@PathVariable Long id) throws UserNotFoundException {
    log.info("Admin deleting user: id={}", id);
    this.deleteUserUseCase.delete(id);
    log.info("User deleted successfully: id={}", id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/retry-email")
  @JustAdminUser
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

  @PostMapping("/{id}/retry-setup-email")
  @JustAdminUser
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

  @PutMapping("/me/password")
  @JustAccessToken
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal Jwt jwt) {
    String email = jwt.getSubject();
    log.info("User changing password: email={}", email);
    changePasswordUseCase.changePassword(email, request.currentPassword(), request.newPassword());
    log.info("Password changed successfully: email={}", email);
    return ResponseEntity.ok().build();
  }
}
