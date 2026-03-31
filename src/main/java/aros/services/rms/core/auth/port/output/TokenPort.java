/* (C) 2026 */
package aros.services.rms.core.auth.port.output;

import aros.services.rms.core.user.domain.User;

public interface TokenPort {
  String generateAccessToken(User user);

  String generateTFAToken(User user);

  String generateRefreshToken(User user);
}
