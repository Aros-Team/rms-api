/* (C) 2026 */

package aros.services.rms.core.auth.application.dto;

/** Sealed interface representing authentication results (success or requires 2FA). */
public sealed interface AuthResult permits AuthResult.Success, AuthResult.RequiresTFA {
  /**
   * Gets the username from the auth result.
   *
   * @return the username string
   */
  String username();

  /**
   * Gets the type of auth result.
   *
   * @return the auth result type
   */
  AuthResultType type();

  /**
   * Successful authentication result with access and refresh tokens.
   *
   * @param username the authenticated username
   * @param acessToken the access token string
   * @param refreshToken the refresh token string
   */
  record Success(String username, String acessToken, String refreshToken) implements AuthResult {
    @Override
    public AuthResultType type() {
      return AuthResultType.SUCCESS;
    }
  }

  /**
   * Result indicating that two-factor authentication is required.
   *
   * @param username the username requesting TFA
   * @param acessToken the TFA token string
   */
  record RequiresTFA(String username, String acessToken) implements AuthResult {
    @Override
    public AuthResultType type() {
      return AuthResultType.TFA_REQUIRED;
    }
  }
}
