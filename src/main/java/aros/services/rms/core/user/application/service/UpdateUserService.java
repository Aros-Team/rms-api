/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.application.service.ExclusiveAreaPolicy;
import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.notification.domain.NotificationCommand;
import aros.services.rms.core.notification.domain.NotificationSeverity;
import aros.services.rms.core.notification.domain.port.input.NotificationUseCase;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Implementation of user update use case for modifying user information. */
@Slf4j
@Service
public class UpdateUserService implements UpdateUserUseCase {
  private final UserRepositoryPort userPort;
  private final AreaRepositoryPort areaPort;
  private final SalaryHistoryRepositoryPort salaryHistoryPort;
  private final NotificationUseCase notificationUseCase;

  /**
   * Creates a service to update users.
   *
   * @param userPort repository for user operations
   * @param areaPort repository for area operations
   * @param salaryHistoryPort port for salary history operations
   * @param notificationUseCase use case for notifying the worker about new modules
   */
  public UpdateUserService(
      UserRepositoryPort userPort,
      AreaRepositoryPort areaPort,
      SalaryHistoryRepositoryPort salaryHistoryPort,
      NotificationUseCase notificationUseCase) {
    this.userPort = userPort;
    this.areaPort = areaPort;
    this.salaryHistoryPort = salaryHistoryPort;
    this.notificationUseCase = notificationUseCase;
  }

  @Override
  public User update(Long userId, UpdateUserInfo info) {
    User user =
        this.userPort
            .findById(new UserId(userId))
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    if (!areaPort.existsAllByIds(info.areas())) {
      throw new AreaNotFoundException("No se pudo encontrar alguna de las áreas referenciadas.");
    }

    ExclusiveAreaPolicy.assertAssignable(
        areaPort.findByIdIn(info.areas().stream().map(AreaId::value).toList()));

    user.updateInfo(info.document(), info.name(), info.address(), info.phone());
    user.setJobTitle(info.jobTitle());
    final Set<Long> previousAreaIds =
        user.getAssignedAreas().stream().map(AreaId::value).collect(Collectors.toSet());
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

    User saved = this.userPort.save(user);
    notifyNewModules(saved, previousAreaIds, info.areas());
    return saved;
  }

  /** Best-effort notification so the worker reloads and loads the newly assigned modules. */
  private void notifyNewModules(User saved, Set<Long> previous, Set<AreaId> current) {
    List<Long> addedIds =
        current.stream().map(AreaId::value).filter(areaId -> !previous.contains(areaId)).toList();
    if (addedIds.isEmpty()) {
      return;
    }
    try {
      String names =
          areaPort.findByIdIn(addedIds).stream()
              .map(Area::getName)
              .sorted()
              .collect(Collectors.joining(", "));
      notificationUseCase.create(
          new NotificationCommand(
              "WORKER_MODULE_ASSIGNED",
              NotificationSeverity.INFO,
              "Nuevos módulos disponibles",
              "Se te asignó: " + names + ". Toca para cargar tus módulos.",
              "/worker",
              null,
              saved.getId().value()));
    } catch (RuntimeException exception) {
      log.warn("Could not notify worker {} about new areas", saved.getId().value(), exception);
    }
  }
}
