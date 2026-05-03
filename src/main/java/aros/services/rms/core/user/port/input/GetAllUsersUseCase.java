/* (C) 2026 */

package aros.services.rms.core.user.port.input;

import aros.services.rms.core.user.domain.User;
import java.util.List;

/** Input port for retrieving all users. */
public interface GetAllUsersUseCase {
  /**
   * Retrieves all users.
   *
   * @return list of all users
   */
  List<User> getAll();
}
