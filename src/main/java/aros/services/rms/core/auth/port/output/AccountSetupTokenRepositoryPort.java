/* (C) 2026 */

package aros.services.rms.core.auth.port.output;

import aros.services.rms.core.auth.domain.AccountSetupToken;
import aros.services.rms.core.user.domain.UserId;
import java.util.Optional;

/** Output port for account setup token persistence operations. */
public interface AccountSetupTokenRepositoryPort {
  /**
   * Saves an account setup token to the repository.
   *
   * @param token the account setup token to save
   * @return the saved account setup token
   */
  AccountSetupToken save(AccountSetupToken token);

  /**
   * Finds an account setup token by its hash.
   *
   * @param tokenHash the token hash
   * @return Optional containing the account setup token if found
   */
  Optional<AccountSetupToken> findByTokenHash(String tokenHash);

  /**
   * Finds an account setup token by hash and user.
   *
   * @param tokenHash the token hash
   * @param userId the user identifier
   * @return Optional containing the account setup token if found
   */
  Optional<AccountSetupToken> findByTokenHashAndUserId(String tokenHash, UserId userId);

  /**
   * Deletes all account setup tokens for a user.
   *
   * @param userId the user identifier
   */
  void deleteByUserId(UserId userId);
}
