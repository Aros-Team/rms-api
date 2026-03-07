/* (C) 2026 */
package aros.services.rms.core.auth.port.output;

import java.util.Optional;

import aros.services.rms.core.auth.domain.RefreshToken;
import aros.services.rms.core.user.domain.UserId;

public interface RefreshTokenRepositoryPort {
    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByUserId(UserId userId);

    void revokeByTokenHash(String tokenHash);

    void revokeAllByUserId(UserId userId);
}
