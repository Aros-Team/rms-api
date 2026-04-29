/* (C) 2026 */

package aros.services.rms.core.auth.port.output;

import aros.services.rms.core.auth.application.dto.AuthUser;
import aros.services.rms.core.user.domain.UserWithAreas;
import java.util.Optional;

/** Output port for retrieving current authenticated user information. */
public interface CurrentUserPort {
  /**
   * Fetches current user information with assigned areas.
   *
   * @return optional user with areas info
   */
  Optional<UserWithAreas> fetchCurrentUserInfo();

  /**
   * Gets the authenticated user.
   *
   * @return optional auth user
   */
  Optional<AuthUser> getAuthUser();
}
