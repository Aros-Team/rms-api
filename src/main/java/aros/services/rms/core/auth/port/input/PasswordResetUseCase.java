/* (C) 2026 */
package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.exception.UserNotFoundException;

public interface PasswordResetUseCase {
  void requestPasswordReset(String email) throws UserNotFoundException;

  void resetPassword(String token, String newPassword);
}
