package aros.services.rms.core.twofactor.port.output;

import java.util.Optional;

import aros.services.rms.core.twofactor.domain.TwoFactorCode;
import aros.services.rms.core.user.domain.UserId;

public interface TwoFactorCodeRepositoryPort {
    TwoFactorCode save(TwoFactorCode code);

    Optional<TwoFactorCode> findValidCode(UserId userId, String codeHash);
}
