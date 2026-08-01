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

  /**
   * Retrieves workers whose name or document contains the given string (case-insensitive).
   *
   * @param search the search term
   * @return list of matching workers
   */
  List<User> getAllBySearch(String search);

  /**
   * Retrieves workers whose document contains the given string (case-insensitive).
   *
   * @param document the document substring
   * @return list of matching workers
   */
  List<User> getAllByDocumentContainingIgnoreCase(String document);
}
