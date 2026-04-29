/* (C) 2026 */

package aros.services.rms.core.user.port.input;

/** Input port for retrying user registration email operations. */
public interface RetryUserEmailUseCase {
  /**
   * Retries sending registration email.
   *
   * @param userId the user ID
   * @return true if email was sent successfully
   */
  boolean retrySendRegistrationEmail(Long userId);
}
