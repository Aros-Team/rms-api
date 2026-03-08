package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.exception.InvalidRefreshToken;

public interface RefreshTokensUseCase {
    public AuthResult refresh(String refreshTokenHash) throws InvalidRefreshToken;
}
