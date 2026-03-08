/* (C) 2026 */
package aros.services.rms.core.twofactor.port.output;

import aros.services.rms.core.twofactor.domain.TwoFactorCode;
import aros.services.rms.core.user.domain.UserId;
import java.util.Optional;

public interface TwoFactorCodeRepositoryPort {
  TwoFactorCode save(TwoFactorCode code);

  Optional<TwoFactorCode> findValidCode(UserId userId, String codeHash);
}
