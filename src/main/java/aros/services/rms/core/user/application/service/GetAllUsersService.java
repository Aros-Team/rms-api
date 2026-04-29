/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.input.GetAllUsersUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.List;

/** Implementation of use case to retrieve all users. */
public class GetAllUsersService implements GetAllUsersUseCase {

  private final UserRepositoryPort userRepositoryPort;

  /**
   * Creates a service to retrieve all users.
   *
   * @param userRepositoryPort repository for user operations
   */
  public GetAllUsersService(UserRepositoryPort userRepositoryPort) {
    this.userRepositoryPort = userRepositoryPort;
  }

  @Override
  public List<User> getAll() {
    return userRepositoryPort.findAll();
  }
}
