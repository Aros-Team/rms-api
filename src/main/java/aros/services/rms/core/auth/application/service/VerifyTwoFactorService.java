/* (C) 2026 */

package aros.services.rms.core.auth.application.service;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.TwoFactorCredentials;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.domain.RefreshToken;
import aros.services.rms.core.auth.port.input.VerifyTwoFactorUseCase;
import aros.services.rms.core.auth.port.output.RefreshTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.TokenPort;
import aros.services.rms.core.device.domain.Device;
import aros.services.rms.core.device.port.output.DeviceRepositoryPort;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.twofactor.domain.TwoFactorCode;
import aros.services.rms.core.twofactor.port.output.TwoFactorCodeRepositoryPort;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/** Implementation of two-factor verification and device registration. */
public class VerifyTwoFactorService implements VerifyTwoFactorUseCase {
  private final UserRepositoryPort userPort;
  private final HashServicePort hashPort;
  private final TwoFactorCodeRepositoryPort tfaPort;
  private final DeviceRepositoryPort devicePort;
  private final RefreshTokenRepositoryPort refreshTokenPort;
  private final TokenPort tokenPort;

  /**
   * Creates a new VerifyTwoFactorService instance.
   *
   * @param userPort the user repository port
   * @param hashPort the hash service port
   * @param tfaPort the 2FA code repository port
   * @param devicePort the device repository port
   * @param refreshTokenPort the refresh token repository port
   * @param tokenPort the token port
   */
  public VerifyTwoFactorService(
      UserRepositoryPort userPort,
      HashServicePort hashPort,
      TwoFactorCodeRepositoryPort tfaPort,
      DeviceRepositoryPort devicePort,
      RefreshTokenRepositoryPort refreshTokenPort,
      TokenPort tokenPort) {
    this.userPort = userPort;
    this.hashPort = hashPort;
    this.tfaPort = tfaPort;
    this.devicePort = devicePort;
    this.refreshTokenPort = refreshTokenPort;
    this.tokenPort = tokenPort;
  }

  /**
   * Verifies a two-factor authentication code.
   *
   * @param credentials the 2FA credentials including username and code
   * @return the authentication result if code is valid
   * @throws InvalidCredentialsException if code is invalid or expired
   */
  @Override
  public AuthResult verify(TwoFactorCredentials credentials) throws InvalidCredentialsException {
    User user =
        userPort
            .findByEmail(credentials.username().value())
            .orElseThrow(InvalidCredentialsException::new);

    String codeHash = hashPort.hash(credentials.code());

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

    return generateAuthResult(credentials.username().value(), user);
  }

  private AuthResult generateAuthResult(String username, User user) {
    String accessToken = tokenPort.generateAccessToken(user);
    String refreshToken = tokenPort.generateRefreshToken(user);

    String refreshHash = hashPort.hash(refreshToken);
    Instant now = Instant.now();

    RefreshToken refreshTokenDomain =
        new RefreshToken(
            null, user.getId(), refreshHash, now.plus(7, ChronoUnit.DAYS), false, Instant.now());

    refreshTokenPort.save(refreshTokenDomain);

    return new AuthResult.Success(username, accessToken, refreshToken);
  }
}
