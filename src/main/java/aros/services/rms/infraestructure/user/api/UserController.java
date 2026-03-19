package aros.services.rms.infraestructure.user.api;

import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.port.input.ChangePasswordUseCase;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.input.GetAllUsersUseCase;
import aros.services.rms.infraestructure.share.security.JustAccessToken;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.user.api.dto.ChangePasswordRequest;
import aros.services.rms.infraestructure.user.api.dto.UserRegisterRequest;
import aros.services.rms.infraestructure.user.api.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/users")
public class UserController {
  private final CreateUserUseCase createUserUseCase;
  private final ChangePasswordUseCase changePasswordUseCase;
  private final GetAllUsersUseCase getAllUsersUseCase;

  @GetMapping
  @JustAdminUser
  public ResponseEntity<List<UserResponse>> getAll() {
    List<UserResponse> users =
        getAllUsersUseCase.getAll().stream().map(UserResponse::fromDomain).toList();
    return ResponseEntity.ok(users);
  }

  @PostMapping
  @JustAdminUser
  public ResponseEntity<Void> register(@Valid @RequestBody UserRegisterRequest request)
      throws UserAlreadyExistsException {
    this.createUserUseCase.create(request.toCreateUserInfo());

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PutMapping("/me/password")
  @JustAccessToken
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal Jwt jwt) {
    String email = jwt.getSubject();
    changePasswordUseCase.changePassword(email, request.currentPassword(), request.newPassword());
    return ResponseEntity.ok().build();
  }
}
