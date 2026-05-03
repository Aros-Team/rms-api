/* (C) 2026 */

package aros.services.rms.core.user.port.output;

import aros.services.rms.core.user.domain.UserRole;

/** Output port for admin-specific repository operations. */
public interface AdminRepositoryPort {
  /**
   * Counts users by role.
   *
   * @param role the role to count
   * @return number of users with the given role
   */
  long countByRole(UserRole role);
}
