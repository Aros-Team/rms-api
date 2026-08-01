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

  /**
   * Finds workers whose name contains the given string (case-insensitive).
   *
   * @param name the name substring
   * @return list of matching workers
   */
  List<User> findByNameContainingIgnoreCase(String name);

  /**
   * Finds workers whose document contains the given string (case-insensitive).
   *
   * @param document the document substring
   * @return list of matching workers
   */
  List<User> findByDocumentContainingIgnoreCase(String document);
}
