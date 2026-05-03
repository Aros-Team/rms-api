/* (C) 2026 */

package aros.services.rms.core.email.port.input;

import aros.services.rms.core.user.domain.UserEmail;

/** Input port for sending welcome emails to new users. */
public interface WelcomeEmailUseCase {
  /**
   * Sends a welcome email.
   *
   * @param destination recipient email
   * @param welcomeToken the welcome token
   * @param userName the user's name
   * @param templateName the email template name
   */
  void sendWelcomeEmail(
      UserEmail destination, String welcomeToken, String userName, String templateName);
}
