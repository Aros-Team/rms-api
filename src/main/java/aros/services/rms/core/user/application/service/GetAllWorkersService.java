/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.input.GetAllWorkersUseCase;
import aros.services.rms.core.user.port.output.WorkerRepositoryPort;
import java.util.List;

/** Implementation of use case to retrieve all workers. */
public class GetAllWorkersService implements GetAllWorkersUseCase {

  private final WorkerRepositoryPort workerRepositoryPort;

  /**
   * Creates a service to retrieve all workers.
   *
   * @param workerRepositoryPort repository for worker operations
   */
  public GetAllWorkersService(WorkerRepositoryPort workerRepositoryPort) {
    this.workerRepositoryPort = workerRepositoryPort;
  }

  @Override
  public List<User> getAll() {
    return workerRepositoryPort.findAllWorkers();
  }
}
