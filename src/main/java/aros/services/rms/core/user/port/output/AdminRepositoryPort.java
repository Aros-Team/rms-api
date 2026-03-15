/* (C) 2026 */
package aros.services.rms.core.user.port.output;

import aros.services.rms.core.user.domain.UserRole;

public interface AdminRepositoryPort {
  long countByRole(UserRole role);
}
