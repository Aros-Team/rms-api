/* (C) 2026 */
package aros.services.rms.core.auth.port.output;

import aros.services.rms.core.auth.domain.AuthToken;
import aros.services.rms.core.user.domain.User;

public interface TokenPort {
  AuthToken generateToken(User user);

  AuthToken generateTFAToken(User user);
}
