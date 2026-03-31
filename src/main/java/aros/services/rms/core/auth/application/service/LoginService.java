package aros.services.rms.core.auth.application.service;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.Credentials;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.domain.RefreshToken;
import aros.services.rms.core.auth.port.input.LoginUseCase;
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
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class LoginService implements LoginUseCase {
  private final PasswordEncoderPort passwordPort;
  private final UserRepositoryPort userPort;
  private final DeviceRepositoryPort devicePort;
  private final TfaCodeGeneratorPort tfaCodeGeneratorPort;
  private final TwoFactorCodeRepositoryPort tfaPort;
  private final TwoFactorAuthEmailUseCase emailPort;
  private final HashServicePort hashServicePort;
  private final RefreshTokenRepositoryPort refreshTokenPort;
  private final TokenPort tokenPort;

  public LoginService(
      PasswordEncoderPort passwordPort,
      UserRepositoryPort userPort,
      DeviceRepositoryPort devicePort,
      TfaCodeGeneratorPort tfaCodeGeneratorPort,
      TwoFactorCodeRepositoryPort tfaPort,
      TwoFactorAuthEmailUseCase emailPort,
      HashServicePort hashServicePort,
      RefreshTokenRepositoryPort refreshTokenPort,
      TokenPort tokenPort) {
    this.passwordPort = passwordPort;
    this.userPort = userPort;
    this.devicePort = devicePort;
    this.tfaCodeGeneratorPort = tfaCodeGeneratorPort;
    this.tfaPort = tfaPort;
    this.emailPort = emailPort;
    this.hashServicePort = hashServicePort;
    this.refreshTokenPort = refreshTokenPort;
    this.tokenPort = tokenPort;
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
      return authenticateFull(user);
    } else {
      return authenticateRequireTFA(user);
    }
  }

  private AuthResult.Success authenticateFull(User user) {
    AuthResult.Success result = createSuccess(user);
    String refreshHash = hashServicePort.hash(result.refreshToken());
    Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

    RefreshToken refreshToken =
        new RefreshToken(null, user.getId(), refreshHash, expiresAt, Instant.now());

    refreshTokenPort.save(refreshToken);

    return result;
  }

  private AuthResult.RequiresTFA authenticateRequireTFA(User user) {
    String code = tfaCodeGeneratorPort.generateCode(6);
    TwoFactorCode TFACode = createVerificationCode(user, code);

    tfaPort.save(TFACode);
    emailPort.sendTwoFactorCode(user.getEmail(), code);

    return createRequireTFA(user);
  }

  private AuthResult.RequiresTFA createRequireTFA(User user) {
    return new AuthResult.RequiresTFA(user.getEmail().value(), tokenPort.generateTFAToken(user));
  }

  private AuthResult.Success createSuccess(User user) {
    return new AuthResult.Success(
        user.getEmail().value(),
        tokenPort.generateAccessToken(user),
        tokenPort.generateRefreshToken(user));
  }

  private TwoFactorCode createVerificationCode(User user, String code) {
    String codeHash = hashServicePort.hash(code);

    TwoFactorCode twoFactorCode =
        new TwoFactorCode(
            null,
            user.getId(),
            codeHash,
            Instant.now(),
            Instant.now().plus(50, ChronoUnit.MINUTES),
            null);

    return twoFactorCode;
  }
}
