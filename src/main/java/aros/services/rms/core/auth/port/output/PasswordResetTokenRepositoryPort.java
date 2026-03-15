/* (C) 2026 */
package aros.services.rms.core.auth.port.output;

import aros.services.rms.core.auth.domain.PasswordResetToken;
import aros.services.rms.core.user.domain.UserId;
import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {
  PasswordResetToken save(PasswordResetToken token);

  Optional<PasswordResetToken> findByTokenHashAndUserId(String tokenHash, UserId userId);

  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  void deleteByUserId(UserId userId);
}
