package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.AuthFinalResult;
import aros.services.rms.core.auth.application.exception.InvalidRefreshTokenException;

public interface RefreshTokensUseCase {
  AuthFinalResult refresh(String refreshTokenHash) throws InvalidRefreshTokenException;
}
