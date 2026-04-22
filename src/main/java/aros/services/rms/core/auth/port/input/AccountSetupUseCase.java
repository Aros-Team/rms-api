/* (C) 2026 */
package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.exception.AccountSetupTokenExpiredException;
import aros.services.rms.core.auth.application.exception.AccountSetupTokenInvalidException;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.auth.api.dto.SetupAccountValidationResponse;

public interface AccountSetupUseCase {
  void setupPassword(String token, String newPassword, String name, String document);

  void requestSetupEmail(String email) throws UserNotFoundException;

  void deleteExistingTokens(UserId userId);

  SetupAccountValidationResponse validateToken(String token)
      throws AccountSetupTokenInvalidException, AccountSetupTokenExpiredException;
}
