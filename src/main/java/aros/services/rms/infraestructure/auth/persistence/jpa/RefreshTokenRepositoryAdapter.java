/* (C) 2026 */
package aros.services.rms.infraestructure.auth.persistence.jpa;

import aros.services.rms.core.auth.domain.RefreshToken;
import aros.services.rms.core.auth.port.output.RefreshTokenRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adapter for refresh token repository. */
@Repository
@RequiredArgsConstructor
@Transactional
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {
  private final JpaRefreshTokenRepository internal;

  private final RefreshTokenPersistenceMapper mapper;

  @Override
  public RefreshToken save(RefreshToken token) {
    return mapper.toDomain(internal.save(mapper.toEntity(token)));
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return internal.findByTokenHash(tokenHash).map(mapper::toDomain);
  }

  @Override
  public Optional<RefreshToken> findByUserId(UserId userId) {
    return internal.findByUserId(userId.value()).map(mapper::toDomain);
  }

  @Override
  public void revokeByTokenHash(String tokenHash) {
    internal
        .findByTokenHash(tokenHash)
        .ifPresent(
            entity -> {
              entity.setRevoked(true);
              internal.save(entity);
            });
  }

  @Override
  public void revokeAllByUserId(UserId userId) {
    internal
        .findByUserId(userId.value())
        .ifPresent(
            entity -> {
              entity.setRevoked(true);
              internal.save(entity);
            });
  }
}
