/* (C) 2026 */

package aros.services.rms.core.user.port.input;

import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.dto.CreateUserInfo;

/** Input port for user creation operations. */
public interface CreateUserUseCase {
  /**
   * Creates a new user.
   *
   * @param info user creation information
   * @return result containing created user and raw password
   * @throws UserAlreadyExistsException if user already exists
   */
  CreateUserResult create(CreateUserInfo info) throws UserAlreadyExistsException;

  record CreateUserResult(User user, String rawPassword) {}
}
