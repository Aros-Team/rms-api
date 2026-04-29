/* (C) 2026 */

package aros.services.rms.core.email.port.input;

import aros.services.rms.core.user.domain.UserEmail;

/** Input port for sending registration notification emails. */
public interface RegistrationEmailUseCase {
  /**
   * Sends a registration email.
   *
   * @param destination recipient email
   * @param message the message content
   */
  void sendRegistrationMail(UserEmail destination, String message);
}
