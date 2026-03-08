/* (C) 2026 */
package aros.services.rms.core.email.port.input;

import aros.services.rms.core.user.domain.UserEmail;

public interface TwoFactorAuthEmailUseCase {
  void sendTwoFactorCode(UserEmail email, String code);
}
