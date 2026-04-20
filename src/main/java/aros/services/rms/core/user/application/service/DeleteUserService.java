/* (C) 2026 */
package aros.services.rms.core.user.application.service;

import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.input.DeleteUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserService implements DeleteUserUseCase {
  private final UserRepositoryPort userPort;

  public DeleteUserService(UserRepositoryPort userPort) {
    this.userPort = userPort;
  }

  @Override
  public void delete(Long userId) {
    User user =
        this.userPort
            .findById(new UserId(userId))
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    user.setDeletedAt(Instant.now());
    this.userPort.save(user);
  }
}
