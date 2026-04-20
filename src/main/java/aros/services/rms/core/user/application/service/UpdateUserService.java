/* (C) 2026 */
package aros.services.rms.core.user.application.service;

import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;
import aros.services.rms.core.user.port.input.UpdateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserService implements UpdateUserUseCase {
  private final UserRepositoryPort userPort;

  public UpdateUserService(UserRepositoryPort userPort) {
    this.userPort = userPort;
  }

  @Override
  public User update(Long userId, UpdateUserInfo info) {
    User user =
        this.userPort
            .findById(new aros.services.rms.core.user.domain.UserId(userId))
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    user.updateInfo(info.name(), info.address(), info.phone());

    if (!user.getEmail().value().equals(info.email().value())) {
      user.changeEmail(info.email());
    }

    return this.userPort.save(user);
  }
}
