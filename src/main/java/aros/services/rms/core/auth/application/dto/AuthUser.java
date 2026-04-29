/* (C) 2026 */

package aros.services.rms.core.auth.application.dto;

import aros.services.rms.core.user.domain.UserRole;
import java.util.List;

/** Record containing authentication-specific user information. */
public record AuthUser(String username, UserRole role, List<Long> areaIds) {
  //
}
