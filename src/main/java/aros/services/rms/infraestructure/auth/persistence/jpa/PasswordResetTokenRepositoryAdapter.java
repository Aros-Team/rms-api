/* (C) 2026 */
package aros.services.rms.infraestructure.auth.persistence.jpa;

import aros.services.rms.core.auth.domain.PasswordResetToken;
import aros.services.rms.core.auth.port.output.PasswordResetTokenRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adapter for password reset token repository. */
@Repository
@RequiredArgsConstructor
@Transactional
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {
  private final JpaPasswordResetTokenRepository internal;

  private final PasswordResetTokenMapper mapper;

  @Override
  public PasswordResetToken save(PasswordResetToken token) {
    return mapper.toDomain(internal.save(mapper.toEntity(token)));
  }

  @Override
  public Optional<PasswordResetToken> findByTokenHashAndUserId(String tokenHash, UserId userId) {
    return internal.findByTokenHashAndUserId(tokenHash, userId.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
    return internal.findByTokenHash(tokenHash).map(mapper::toDomain);
  }

  @Override
  public void deleteByUserId(UserId userId) {
    internal.deleteByUserId(userId.value());
  }
}
