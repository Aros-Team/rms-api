package aros.services.rms.core.auth.application.service;

import aros.services.rms.core.auth.application.dto.AuthFinalResult;
import aros.services.rms.core.auth.application.exception.InvalidRefreshToken;
import aros.services.rms.core.auth.domain.RefreshToken;
import aros.services.rms.core.auth.port.input.RefreshTokensUseCase;
import aros.services.rms.core.auth.port.output.RefreshTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.TokenPort;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class RefreshTokenService implements RefreshTokensUseCase {
  private final HashServicePort hashPort;
  private final UserRepositoryPort userPort;
  private final RefreshTokenRepositoryPort refreshTokenPort;
  private final TokenPort tokenPort;
  private final HashServicePort hashServicePort;

  public RefreshTokenService(
      HashServicePort hashPort,
      UserRepositoryPort userPort,
      RefreshTokenRepositoryPort refreshTokenPort,
      TokenPort tokenPort,
      HashServicePort hashServicePort) {
    this.hashPort = hashPort;
    this.userPort = userPort;
    this.refreshTokenPort = refreshTokenPort;
    this.tokenPort = tokenPort;
    this.hashServicePort = hashServicePort;
  }

  @Override
  public AuthFinalResult refresh(String token) throws InvalidRefreshToken {
    String tokenHash = hashPort.hash(token);
    RefreshToken refreshToken =
        refreshTokenPort.findByTokenHash(tokenHash).orElseThrow(InvalidRefreshToken::new);

    if (refreshToken.isExpired() || refreshToken.isRevoked()) {
      throw new InvalidRefreshToken();
    }

    User user = this.userPort.findById(refreshToken.getUserId()).orElseThrow();

    refreshToken.revoke();
    this.refreshTokenPort.save(refreshToken);

    String accessToken = tokenPort.generateAccessToken(user);
    String newRefreshToken = tokenPort.generateRefreshToken(user);
    String refreshHash = hashServicePort.hash(newRefreshToken);

    RefreshToken refreshTokenDomain =
        new RefreshToken(
            null, user.getId(), refreshHash, Instant.now().plus(7, ChronoUnit.DAYS), Instant.now());

    refreshTokenPort.save(refreshTokenDomain);

    return new AuthFinalResult(user.getEmail().value(), accessToken, newRefreshToken);
  }
}
