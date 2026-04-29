/* (C) 2026 */

package aros.services.rms.core.email.port.input;

import aros.services.rms.core.user.domain.UserEmail;

/** Input port for sending password reset emails. */
public interface PasswordResetEmailUseCase {
  /**
   * Sends a password reset email.
   *
   * @param destination recipient email
   * @param resetToken the reset token
   */
  void sendPasswordResetEmail(UserEmail destination, String resetToken);
}
