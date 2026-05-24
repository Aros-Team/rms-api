/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.user.application.exception.InvalidSalaryException;
import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.Salary;
import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;
import aros.services.rms.core.user.port.input.UpdateUserUseCase;
import aros.services.rms.core.user.port.output.SalaryHistoryRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Instant;
import org.springframework.stereotype.Service;

/** Implementation of user update use case for modifying user information. */
@Service
public class UpdateUserService implements UpdateUserUseCase {
  private final UserRepositoryPort userPort;
  private final AreaRepositoryPort areaPort;
  private final SalaryHistoryRepositoryPort salaryHistoryPort;

  /**
   * Creates a service to update users.
   *
   * @param userPort repository for user operations
   * @param areaPort repository for area operations
   * @param salaryHistoryPort port for salary history operations
   */
  public UpdateUserService(
      UserRepositoryPort userPort,
      AreaRepositoryPort areaPort,
      SalaryHistoryRepositoryPort salaryHistoryPort) {
    this.userPort = userPort;
    this.areaPort = areaPort;
    this.salaryHistoryPort = salaryHistoryPort;
  }

  @Override
  public User update(Long userId, UpdateUserInfo info) {
    User user =
        this.userPort
            .findById(new UserId(userId))
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    if (!areaPort.existsAllByIds(info.areas())) {
      throw new AreaNotFoundException("No se pudo encontrar alguna de areas referenciadas.");
    }

    user.updateInfo(info.document(), info.name(), info.address(), info.phone());
    user.reAssinngAreas(info.areas());

    if (!user.getEmail().value().equals(info.email().value())) {
      user.changeEmail(info.email());
    }

    if (info.salary() != null) {
      if (user.getSalary() == null || !user.getSalary().isEqualTo(info.salary())) {
        if (info.reason() == null || info.reason().isBlank()) {
          throw new InvalidSalaryException("La razón es obligatoria cuando se cambia el salario");
        }
        Salary oldSalary = user.getSalary();
        user.setSalary(info.salary());
        SalaryHistoryEntry historyEntry =
            new SalaryHistoryEntry(
                null,
                user.getId(),
                oldSalary,
                info.salary(),
                Instant.now(),
                info.reason(),
                info.observations());
        this.salaryHistoryPort.save(historyEntry);
      }
    }

    return this.userPort.save(user);
  }
}
