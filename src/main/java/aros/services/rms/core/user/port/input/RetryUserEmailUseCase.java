/* (C) 2026 */
package aros.services.rms.core.user.port.input;

public interface RetryUserEmailUseCase {
  boolean retrySendRegistrationEmail(Long userId);
}
