/* (C) 2026 */

package aros.services.rms.core.email.port.output;

import aros.services.rms.core.email.domain.Email;

/** Output port for email sending operations. */
public interface EmailServicePort {
  /**
   * Sends an email asynchronously.
   *
   * @param email the email to send
   */
  void send(Email email);

  /**
   * Sends an email synchronously.
   *
   * @param email the email to send
   * @return true if sent successfully
   */
  boolean sendSync(Email email);
}
