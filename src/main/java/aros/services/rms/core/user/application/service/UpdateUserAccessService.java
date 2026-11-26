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
import aros.services.rms.core.user.application.exception.InvalidUserAccessException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.input.UpdateUserAccessUseCase;
import aros.services.rms.core.user.port.output.AdminRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Applies role, status and area changes with last-admin and self-lockout protection. */
@Slf4j
@RequiredArgsConstructor
public class UpdateUserAccessService implements UpdateUserAccessUseCase {

  private final UserRepositoryPort userRepositoryPort;
  private final AreaRepositoryPort areaRepositoryPort;
  private final AdminRepositoryPort adminRepositoryPort;
  private final NotificationUseCase notificationUseCase;

  /** {@inheritDoc} */
  @Override
  public User changeRole(Long id, UserRole role, Long actorId) {
    if (role == null) {
      throw new InvalidUserAccessException("El rol es obligatorio");
    }
    User user = requireUser(id);
    if (user.getRole() == role) {
      return user;
    }
    if (user.getRole() == UserRole.ADMIN && role != UserRole.ADMIN) {
      assertNotLastAdmin(user);
      assertNotSelf(id, actorId, "You cannot remove your own administrator role");
    }
    user.changeRole(role);
    return userRepositoryPort.save(user);
  }

  /** {@inheritDoc} */
  @Override
  public User changeAreas(Long id, Set<Long> areaIds) {
    User user = requireUser(id);
    Set<Long> requested = areaIds == null ? Set.of() : areaIds;
    Set<AreaId> areas = requested.stream().map(AreaId::of).collect(Collectors.toSet());
    if (!areas.isEmpty() && !areaRepositoryPort.existsAllByIds(areas)) {
      throw new AreaNotFoundException("No se pudo encontrar alguna de las áreas referenciadas.");
    }
    ExclusiveAreaPolicy.assertAssignable(
        areaRepositoryPort.findByIdIn(areas.stream().map(AreaId::value).toList()));
    Set<Long> previous =
        user.getAssignedAreas().stream().map(AreaId::value).collect(Collectors.toSet());
    user.reAssinngAreas(areas);
    User saved = userRepositoryPort.save(user);
    notifyNewModules(saved, previous, areas);
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
          areaRepositoryPort.findByIdIn(addedIds).stream()
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
      log.warn("Could not notify user {} about new areas", idOf(saved), exception);
    }
  }

  private static Long idOf(User user) {
    return user.getId() == null ? null : user.getId().value();
  }

  /** {@inheritDoc} */
  @Override
  public User changeStatus(Long id, UserStatus status, Long actorId) {
    if (status == null) {
      throw new InvalidUserAccessException("El estado es obligatorio");
    }
    if (status != UserStatus.ACTIVE && status != UserStatus.INACTIVE) {
      throw new InvalidUserAccessException("El estado debe ser ACTIVE o INACTIVE");
    }
    User user = requireUser(id);
    if (status == UserStatus.INACTIVE) {
      assertNotSelf(id, actorId, "No puedes desactivar tu propia cuenta");
      if (user.getRole() == UserRole.ADMIN) {
        assertNotLastAdmin(user);
      }
    }
    user.setStatus(status);
    return userRepositoryPort.save(user);
  }

  private User requireUser(Long id) {
    return userRepositoryPort
        .findById(UserId.of(id))
        .orElseThrow(
            () ->
                new aros.services.rms.core.user.application.exception.UserNotFoundException(
                    "User not found with id: " + id));
  }

  private void assertNotLastAdmin(User user) {
    if (adminRepositoryPort.countByRole(UserRole.ADMIN) <= 1) {
      throw new InvalidUserAccessException("Debe quedar al menos un administrador activo");
    }
  }

  private void assertNotSelf(Long id, Long actorId, String message) {
    if (actorId != null && actorId.equals(id)) {
      throw new InvalidUserAccessException(message);
    }
  }
}
