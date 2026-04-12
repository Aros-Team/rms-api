package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.exception.InvalidRefreshTokenException;

public interface RefreshTokensUseCase {
  AuthResult refresh(String refreshTokenHash) throws InvalidRefreshTokenException;
}
