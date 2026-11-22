/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.user.application.exception.InvalidUserAccessException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.input.UpdateUserAccessUseCase;
import aros.services.rms.core.user.port.output.AdminRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/** Applies role, status and area changes with last-admin and self-lockout protection. */
@RequiredArgsConstructor
public class UpdateUserAccessService implements UpdateUserAccessUseCase {

  private final UserRepositoryPort userRepositoryPort;
  private final AreaRepositoryPort areaRepositoryPort;
  private final AdminRepositoryPort adminRepositoryPort;

  /** {@inheritDoc} */
  @Override
  public User changeRole(Long id, UserRole role, Long actorId) {
    if (role == null) {
      throw new InvalidUserAccessException("Role is required");
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
      throw new AreaNotFoundException("No se pudo encontrar alguna de areas referenciadas.");
    }
    user.reAssinngAreas(areas);
    return userRepositoryPort.save(user);
  }

  /** {@inheritDoc} */
  @Override
  public User changeStatus(Long id, UserStatus status, Long actorId) {
    if (status == null) {
      throw new InvalidUserAccessException("Status is required");
    }
    if (status != UserStatus.ACTIVE && status != UserStatus.INACTIVE) {
      throw new InvalidUserAccessException("Status must be ACTIVE or INACTIVE");
    }
    User user = requireUser(id);
    if (status == UserStatus.INACTIVE) {
      assertNotSelf(id, actorId, "You cannot deactivate your own account");
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
      throw new InvalidUserAccessException("At least one administrator must remain active");
    }
  }

  private void assertNotSelf(Long id, Long actorId, String message) {
    if (actorId != null && actorId.equals(id)) {
      throw new InvalidUserAccessException(message);
    }
  }
}
