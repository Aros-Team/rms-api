/* (C) 2026 */

package aros.services.rms.core.user.port.input;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import java.util.Set;

/** Use case for safely updating a user's role, status and assigned areas. */
public interface UpdateUserAccessUseCase {

  /**
   * Changes a user's role while preventing the last administrator from being demoted.
   *
   * @param id the target user id
   * @param role the new role
   * @param actorId the administrator performing the change
   * @return the updated user
   */
  User changeRole(Long id, UserRole role, Long actorId);

  /**
   * Replaces a user's assigned areas.
   *
   * @param id the target user id
   * @param areaIds the new area ids
   * @return the updated user
   */
  User changeAreas(Long id, Set<Long> areaIds);

  /**
   * Activates or deactivates a user while preventing self-lockout.
   *
   * @param id the target user id
   * @param status the new status
   * @param actorId the administrator performing the change
   * @return the updated user
   */
  User changeStatus(Long id, UserStatus status, Long actorId);
}
