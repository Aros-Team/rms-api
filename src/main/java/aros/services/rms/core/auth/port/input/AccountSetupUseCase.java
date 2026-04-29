/* (C) 2026 */

package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.exception.AccountSetupTokenExpiredException;
import aros.services.rms.core.auth.application.exception.AccountSetupTokenInvalidException;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.auth.api.dto.SetupAccountValidationResponse;

/** Input port for account setup operations (password configuration for new users). */
public interface AccountSetupUseCase {
  /**
   * Sets up a new user's password using an account setup token.
   *
   * @param token the account setup token
   * @param newPassword the new password to set
   * @param name the user's name
   * @param document the user's document number
   */
  void setupPassword(String token, String newPassword, String name, String document);

  /**
   * Requests an account setup email for a new user.
   *
   * @param email the user's email address
   * @throws UserNotFoundException if no user exists with that email
   */
  void requestSetupEmail(String email) throws UserNotFoundException;

  /**
   * Deletes all existing account setup tokens for a user.
   *
   * @param userId the user identifier
   */
  void deleteExistingTokens(UserId userId);

  /**
   * Validates an account setup token.
   *
   * @param token the token to validate
   * @return validation response with token status
   * @throws AccountSetupTokenInvalidException if token is invalid
   * @throws AccountSetupTokenExpiredException if token has expired
   */
  SetupAccountValidationResponse validateToken(String token)
      throws AccountSetupTokenInvalidException, AccountSetupTokenExpiredException;
}
