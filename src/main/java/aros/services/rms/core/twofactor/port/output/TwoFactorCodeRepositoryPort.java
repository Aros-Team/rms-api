/* (C) 2026 */

package aros.services.rms.core.twofactor.port.output;

import aros.services.rms.core.twofactor.domain.TwoFactorCode;
import aros.services.rms.core.user.domain.UserId;
import java.util.Optional;

/** Output port for two-factor code persistence operations. */
public interface TwoFactorCodeRepositoryPort {
  /**
   * Saves a two-factor code.
   *
   * @param code the code to save
   * @return the saved code
   */
  TwoFactorCode save(TwoFactorCode code);

  /**
   * Finds a valid code for user.
   *
   * @param userId the user ID
   * @param codeHash the hashed code
   * @return optional valid code
   */
  Optional<TwoFactorCode> findValidCode(UserId userId, String codeHash);
}
