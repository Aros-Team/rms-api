/* (C) 2026 */
package aros.services.rms.core.auth.application.service;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.AuthResultType;
import aros.services.rms.core.auth.application.dto.Credentials;
import aros.services.rms.core.auth.application.dto.TwoFactorCredentials;
import aros.services.rms.core.auth.application.dto.UserFullInfo;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.application.exception.InvalidRefreshToken;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.auth.domain.AuthToken;
import aros.services.rms.core.auth.domain.RefreshToken;
import aros.services.rms.core.auth.port.input.GetCurrentAuthUserInfoUseCase;
import aros.services.rms.core.auth.port.input.LoginUseCase;
import aros.services.rms.core.auth.port.input.RefreshTokensUseCase;
import aros.services.rms.core.auth.port.input.VerifyTwoFactorUseCase;
import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.auth.port.output.RefreshTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.TokenPort;
import aros.services.rms.core.device.domain.Device;
import aros.services.rms.core.device.port.output.DeviceRepositoryPort;
import aros.services.rms.core.email.port.input.TwoFactorAuthEmailUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.twofactor.domain.TwoFactorCode;
import aros.services.rms.core.twofactor.port.output.TfaCodeGeneratorPort;
import aros.services.rms.core.twofactor.port.output.TwoFactorCodeRepositoryPort;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class AuthService
    implements LoginUseCase,
        VerifyTwoFactorUseCase,
        RefreshTokensUseCase,
        GetCurrentAuthUserInfoUseCase {
  private final UserRepositoryPort userPort;
  private final DeviceRepositoryPort devicePort;
  private final TokenPort tokenPort;
  private final PasswordEncoderPort passwordPort;
  private final TwoFactorAuthEmailUseCase emailPort;
  private final TwoFactorCodeRepositoryPort tfaPort;
  private final RefreshTokenRepositoryPort refreshTokenPort;
  private final HashServicePort hashServicePort;
  private final TfaCodeGeneratorPort tfaCodeGeneratorPort;
  private final AreaRepositoryPort areaPort;

  public AuthService(
      UserRepositoryPort userPort,
      DeviceRepositoryPort devicePort,
      TokenPort tokenPort,
      PasswordEncoderPort passwordPort,
      TwoFactorAuthEmailUseCase emailPort,
      TwoFactorCodeRepositoryPort tfaPort,
      RefreshTokenRepositoryPort refreshTokenPort,
      HashServicePort hashServicePort,
      TfaCodeGeneratorPort tfaCodeGeneratorPort,
      AreaRepositoryPort areaPort) {
    this.userPort = userPort;
    this.devicePort = devicePort;
    this.tokenPort = tokenPort;
    this.passwordPort = passwordPort;
    this.emailPort = emailPort;
    this.tfaPort = tfaPort;
    this.refreshTokenPort = refreshTokenPort;
    this.hashServicePort = hashServicePort;
    this.tfaCodeGeneratorPort = tfaCodeGeneratorPort;
    this.areaPort = areaPort;
  }

  @Override
  public AuthResult authenticate(Credentials credentials) throws InvalidCredentialsException {
    User user =
        this.userPort
            .findByEmail(credentials.username().value())
            .orElseThrow(InvalidCredentialsException::new);

    if (!passwordPort.validate(credentials.password(), user.getPassword())) {
      throw new InvalidCredentialsException();
    }

    Optional<Device> deviceOpt =
        devicePort.findByUserIdAndHash(user.getId(), credentials.deviceHash());

    if (deviceOpt.isPresent()) {
      return generateAuthResult(AuthResultType.SUCCESS, credentials.username().value(), user);
    }

    return generateAuthResult(AuthResultType.TFA_REQUIRED, credentials.username().value(), user);
  }

  @Override
  public AuthResult verify(TwoFactorCredentials credentials) throws InvalidCredentialsException {
    User user =
        userPort
            .findByEmail(credentials.username().value())
            .orElseThrow(InvalidCredentialsException::new);

    String codeHash = hashServicePort.hash(credentials.code());

    TwoFactorCode twoFactorCode =
        tfaPort.findValidCode(user.getId(), codeHash).orElseThrow(InvalidCredentialsException::new);

    if (twoFactorCode.usedAt() != null || Instant.now().isAfter(twoFactorCode.expiresAt())) {
      throw new InvalidCredentialsException();
    }

    TwoFactorCode usedCode = twoFactorCode.markAsUsed();
    tfaPort.save(usedCode);

    Optional<Device> deviceOpt =
        devicePort.findByUserIdAndHash(user.getId(), credentials.deviceHash());

    if (deviceOpt.isEmpty()) {
      Device newDevice = new Device(null, user.getId(), credentials.deviceHash());
      devicePort.save(newDevice);
    }

    return generateAuthResult(AuthResultType.SUCCESS, credentials.username().value(), user);
  }

  @Override
  public AuthResult refresh(String token) throws InvalidRefreshToken {
    String tokenHash = hashServicePort.hash(token);
    RefreshToken refreshToken =
        refreshTokenPort.findByTokenHash(tokenHash).orElseThrow(InvalidRefreshToken::new);

    if (refreshToken.isExpired() || refreshToken.isRevoked()) {
      throw new InvalidRefreshToken();
    }

    User user = this.userPort.findById(refreshToken.getUserId()).orElseThrow();

    refreshToken.revoke();
    this.refreshTokenPort.save(refreshToken);

    return generateAuthResult(AuthResultType.SUCCESS, user.getEmail().value(), user);
  }

  @Override
  public UserFullInfo getInfo(UserEmail email) throws UserNotFoundException {
    User user =
        userPort
            .findByEmail(email.value())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

    List<Area> areas =
        areaPort.findByIdIn(user.getAssignedAreas().stream().map(a -> a.value()).toList());

    return new UserFullInfo(
        user.getId(),
        user.getDocument(),
        user.getName(),
        user.getEmail(),
        user.getPassword(),
        user.getAddress(),
        user.getPhone(),
        user.getRole(),
        areas);
  }

  private AuthResult generateAuthResult(AuthResultType type, String username, User user) {
    if (type.equals(AuthResultType.TFA_REQUIRED)) {
      String code = tfaCodeGeneratorPort.generateCode(6);
      String codeHash = hashServicePort.hash(code);

      TwoFactorCode twoFactorCode =
          new TwoFactorCode(
              null,
              user.getId(),
              codeHash,
              Instant.now(),
              Instant.now().plus(50, ChronoUnit.MINUTES),
              null);
      tfaPort.save(twoFactorCode);
      emailPort.sendTwoFactorCode(user.getEmail(), code);

      return new AuthResult(
          AuthResultType.TFA_REQUIRED, username, tokenPort.generateTFAToken(user));
    }

    AuthToken token = tokenPort.generateToken(user);

    String refreshHash = hashServicePort.hash(token.getRefresh());
    Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

    RefreshToken refreshToken =
        new RefreshToken(null, user.getId(), refreshHash, expiresAt, Instant.now());

    refreshTokenPort.save(refreshToken);

    return new AuthResult(type, username, token);
  }
}
