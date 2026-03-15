/* (C) 2026 */
package aros.services.rms.core.email.port.input;

import aros.services.rms.core.user.domain.UserEmail;

public interface PasswordResetEmailUseCase {
  void sendPasswordResetEmail(UserEmail destination, String resetToken);
}
