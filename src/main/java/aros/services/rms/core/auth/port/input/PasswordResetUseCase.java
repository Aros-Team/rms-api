/* (C) 2026 */

package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.exception.UserNotFoundException;

/** Input port for password reset operations. */
public interface PasswordResetUseCase {
  /**
   * Requests a password reset email for a user.
   *
   * @param email the user's email address
   * @throws UserNotFoundException if no user exists with that email
   */
  void requestPasswordReset(String email) throws UserNotFoundException;

  /**
   * Resets a user's password using a valid token.
   *
   * @param token the password reset token
   * @param newPassword the new password to set
   */
  void resetPassword(String token, String newPassword);
}
