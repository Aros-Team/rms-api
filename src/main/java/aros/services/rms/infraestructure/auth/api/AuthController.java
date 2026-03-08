/* (C) 2026 */
package aros.services.rms.infraestructure.auth.api;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.Credentials;
import aros.services.rms.core.auth.application.dto.TwoFactorCredentials;
import aros.services.rms.core.auth.application.dto.UserFullInfo;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.application.exception.InvalidRefreshToken;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.auth.port.input.GetCurrentAuthUserInfoUseCase;
import aros.services.rms.core.auth.port.input.LoginUseCase;
import aros.services.rms.core.auth.port.input.RefreshTokensUseCase;
import aros.services.rms.core.auth.port.input.VerifyTwoFactorUseCase;
import aros.services.rms.core.email.domain.Email;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.infraestructure.auth.api.dto.AuthResponse;
import aros.services.rms.infraestructure.auth.api.dto.LoginRequest;
import aros.services.rms.infraestructure.auth.api.dto.UserFullInfoResponse;
import aros.services.rms.infraestructure.auth.api.dto.VerifyTwoFactorRequest;
import aros.services.rms.infraestructure.share.security.JustAccessToken;
import aros.services.rms.infraestructure.share.security.OnlyRefreshToken;
import aros.services.rms.infraestructure.share.security.OnlyTfaTOken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final VerifyTwoFactorUseCase verifyTwoFactorUseCase;
  private final RefreshTokensUseCase refreshTokensUseCase;
  private final GetCurrentAuthUserInfoUseCase getUserInfoUseCase;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request)
      throws InvalidCredentialsException {
    Credentials credentials = new Credentials(
        new UserEmail(request.username()), request.password(), request.deviceHash());

    AuthResult result = loginUseCase.authenticate(credentials);

    AuthResponse response = new AuthResponse(
        result.type().name(),
        result.username(),
        result.token().getAccess(),
        result.token().getRefresh());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/verify")
  @OnlyTfaTOken
  public ResponseEntity<AuthResponse> verifyTfa(@Valid @RequestBody VerifyTwoFactorRequest request,
      @AuthenticationPrincipal Jwt jwt)
      throws InvalidCredentialsException {
    TwoFactorCredentials credentials = new TwoFactorCredentials(
        new UserEmail(jwt.getSubject()), request.code(), request.deviceHash());

    AuthResult result = verifyTwoFactorUseCase.verify(credentials);

    AuthResponse response = new AuthResponse(
        result.type().name(),
        result.username(),
        result.token().getAccess(),
        result.token().getRefresh());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  @OnlyRefreshToken
  public ResponseEntity<AuthResponse> refresh(
      @RequestHeader("Authorization") String token) throws InvalidRefreshToken {
    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }

    AuthResult result = refreshTokensUseCase.refresh(token);

    AuthResponse response = new AuthResponse(
        result.type().name(),
        result.username(),
        result.token().getAccess(),
        result.token().getRefresh());

    return ResponseEntity.ok(response);
  }

  @GetMapping
  @JustAccessToken
  public ResponseEntity<UserFullInfoResponse> me(@AuthenticationPrincipal Jwt auth) throws UserNotFoundException {
    UserFullInfo uInfo = getUserInfoUseCase.getInfo(new UserEmail(auth.getSubject()));

    UserFullInfoResponse uInfoResponse = new UserFullInfoResponse(
      uInfo.id().value(), uInfo.document(), uInfo.name(), uInfo.email().value(), uInfo.password(), uInfo.address(), uInfo.phone(), uInfo.role(), uInfo.areas());

    return ResponseEntity.ok(uInfoResponse);
  }
}
