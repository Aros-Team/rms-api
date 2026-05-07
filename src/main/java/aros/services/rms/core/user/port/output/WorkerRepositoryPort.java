/* (C) 2026 */

package aros.services.rms.core.user.port.output;

import aros.services.rms.core.user.domain.User;
import java.util.List;

/** Output port for worker persistence operations. */
public interface WorkerRepositoryPort {
  /**
   * Retrieves all workers.
   *
   * @return list of all workers
   */
  List<User> findAllWorkers();
}
