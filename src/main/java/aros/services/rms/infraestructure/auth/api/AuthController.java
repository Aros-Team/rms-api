/* (C) 2026 */
package aros.services.rms.infraestructure.auth.api;

import aros.services.rms.core.auth.application.dto.AuthFinalResult;
import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.AuthResultType;
import aros.services.rms.core.auth.application.dto.Credentials;
import aros.services.rms.core.auth.application.dto.TwoFactorCredentials;
import aros.services.rms.core.auth.application.dto.UserFullInfo;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.application.exception.InvalidRefreshToken;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.auth.port.input.GetCurrentAuthUserInfoUseCase;
import aros.services.rms.core.auth.port.input.LoginUseCase;
import aros.services.rms.core.auth.port.input.PasswordResetUseCase;
import aros.services.rms.core.auth.port.input.RefreshTokensUseCase;
import aros.services.rms.core.auth.port.input.VerifyTwoFactorUseCase;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.infraestructure.auth.api.dto.AuthResponse;
import aros.services.rms.infraestructure.auth.api.dto.ForgotPasswordRequest;
import aros.services.rms.infraestructure.auth.api.dto.LoginRequest;
import aros.services.rms.infraestructure.auth.api.dto.ResetPasswordRequest;
import aros.services.rms.infraestructure.auth.api.dto.UserFullInfoResponse;
import aros.services.rms.infraestructure.auth.api.dto.VerifyTwoFactorRequest;
import aros.services.rms.infraestructure.share.security.JustAccessToken;
import aros.services.rms.infraestructure.share.security.OnlyRefreshToken;
import aros.services.rms.infraestructure.share.security.OnlyTfaTOken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@Tag(name = "Auth", description = "User authentication and session management")
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final VerifyTwoFactorUseCase verifyTwoFactorUseCase;
  private final RefreshTokensUseCase refreshTokensUseCase;
  private final GetCurrentAuthUserInfoUseCase getUserInfoUseCase;
  private final PasswordResetUseCase passwordResetUseCase;

  @Operation(
      summary = "User login",
      description =
          "Authenticates user with email and password. Returns access token and refresh token. "
              + "If user has 2FA enabled, returns REQUIRE_2FA type.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Invalid credentials"),
        @ApiResponse(responseCode = "401", description = "Authentication failed")
      })
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request)
      throws InvalidCredentialsException {
    Credentials credentials =
        new Credentials(
            new UserEmail(request.username()), request.password(), request.deviceHash());

    AuthResult result = loginUseCase.authenticate(credentials);
    AuthResponse response = null;

    if (result instanceof AuthResult.Success rs) {
      response =
          new AuthResponse(rs.type().name(), rs.username(), rs.acessToken(), rs.refreshToken());
    } else if (result instanceof AuthResult.RequiresTFA rs) {
      response = new AuthResponse(rs.type().name(), rs.username(), rs.acessToken(), null);
    }

    return switch (response) {
      case null -> ResponseEntity.internalServerError().build();
      default -> ResponseEntity.ok(response);
    };
  }

  @Operation(
      summary = "Verify two-factor authentication",
      description =
          "Verifies the 2FA code sent to user's device. "
              + "Requires a valid TFA token obtained from login.",
      responses = {
        @ApiResponse(responseCode = "200", description = "2FA verification successful"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired code"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @PostMapping("/verify")
  @OnlyTfaTOken
  public ResponseEntity<AuthResponse> verifyTfa(
      @Valid @RequestBody VerifyTwoFactorRequest request, @AuthenticationPrincipal Jwt jwt)
      throws InvalidCredentialsException {
    TwoFactorCredentials credentials =
        new TwoFactorCredentials(
            new UserEmail(jwt.getSubject()), request.code(), request.deviceHash());

    AuthFinalResult result = verifyTwoFactorUseCase.verify(credentials);

    AuthResponse response =
        new AuthResponse(
            AuthResultType.SUCCESS.name(),
            result.username(),
            result.acessToken(),
            result.refreshToken());

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Refresh access token",
      description =
          "Renews the access token using a valid refresh token. "
              + "The refresh token must be sent in the Authorization header.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
      })
  @PostMapping("/refresh")
  @OnlyRefreshToken
  public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String token)
      throws InvalidRefreshToken {
    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }

    AuthFinalResult result = refreshTokensUseCase.refresh(token);
    AuthResponse response =
        new AuthResponse(
            AuthResultType.SUCCESS.name(),
            result.username(),
            result.acessToken(),
            result.refreshToken());

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get current user",
      description =
          "Returns the authenticated user's information "
              + "(obtained from the JWT token in the Authorization header).",
      responses = {
        @ApiResponse(responseCode = "200", description = "User information retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @GetMapping
  @JustAccessToken
  public ResponseEntity<UserFullInfoResponse> me(@AuthenticationPrincipal Jwt auth)
      throws UserNotFoundException {
    UserFullInfo uInfo = getUserInfoUseCase.getInfo(new UserEmail(auth.getSubject()));

    UserFullInfoResponse uInfoResponse =
        new UserFullInfoResponse(
            uInfo.id().value(),
            uInfo.document(),
            uInfo.name(),
            uInfo.email().value(),
            uInfo.password(),
            uInfo.address(),
            uInfo.phone(),
            uInfo.role(),
            uInfo.areas());

    return ResponseEntity.ok(uInfoResponse);
  }

  @Operation(
      summary = "Request password reset",
      description =
          "Sends a password reset email with recovery token to the provided email address.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Recovery email sent"),
        @ApiResponse(responseCode = "400", description = "Invalid email")
      })
  @PostMapping("/forgot-password")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request)
      throws UserNotFoundException {
    passwordResetUseCase.requestPasswordReset(request.email());
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Reset password",
      description =
          "Resets user password using the recovery token sent by email.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Password updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    passwordResetUseCase.resetPassword(request.token(), request.newPassword());
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
