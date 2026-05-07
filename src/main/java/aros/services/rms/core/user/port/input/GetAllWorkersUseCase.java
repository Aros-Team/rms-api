/* (C) 2026 */

package aros.services.rms.core.user.port.input;

import aros.services.rms.core.user.domain.User;
import java.util.List;

/** Input port for retrieving all workers. */
public interface GetAllWorkersUseCase {
  /**
   * Retrieves all users with role WORKER.
   *
   * @return list of all workers
   */
  List<User> getAll();
}
