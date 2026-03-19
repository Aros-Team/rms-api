/* (C) 2026 */
package aros.services.rms.core.user.application.usecases;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.input.GetAllUsersUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.List;

public class GetAllUsersUseCaseImpl implements GetAllUsersUseCase {

  private final UserRepositoryPort userRepositoryPort;

  public GetAllUsersUseCaseImpl(UserRepositoryPort userRepositoryPort) {
    this.userRepositoryPort = userRepositoryPort;
  }

  @Override
  public List<User> getAll() {
    return userRepositoryPort.findAll();
  }
}
