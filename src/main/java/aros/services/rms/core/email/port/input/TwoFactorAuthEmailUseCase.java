/* (C) 2026 */

package aros.services.rms.core.email.port.input;

import aros.services.rms.core.user.domain.UserEmail;

/** Input port for sending two-factor authentication codes. */
public interface TwoFactorAuthEmailUseCase {
  /**
   * Sends a two-factor authentication code.
   *
   * @param email recipient email
   * @param code the authentication code
   */
  void sendTwoFactorCode(UserEmail email, String code);
}
