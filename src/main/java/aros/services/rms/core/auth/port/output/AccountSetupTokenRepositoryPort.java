/* (C) 2026 */
package aros.services.rms.core.auth.port.output;

import aros.services.rms.core.auth.domain.AccountSetupToken;
import aros.services.rms.core.user.domain.UserId;
import java.util.Optional;

public interface AccountSetupTokenRepositoryPort {
  AccountSetupToken save(AccountSetupToken token);

  Optional<AccountSetupToken> findByTokenHash(String tokenHash);

  Optional<AccountSetupToken> findByTokenHashAndUserId(String tokenHash, UserId userId);

  void deleteByUserId(UserId userId);
}
