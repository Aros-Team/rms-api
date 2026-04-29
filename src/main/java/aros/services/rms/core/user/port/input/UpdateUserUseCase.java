/* (C) 2026 */

package aros.services.rms.core.user.port.input;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;

/** Input port for user update operations. */
public interface UpdateUserUseCase {
  /**
   * Updates a user.
   *
   * @param userId the user ID to update
   * @param info updated user information
   * @return the updated user
   */
  User update(Long userId, UpdateUserInfo info);
}
