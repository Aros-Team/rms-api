/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.application.exception.ExclusiveAreaAssignmentException;
import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.user.application.exception.InvalidUserAccessException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.output.AdminRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link UpdateUserAccessService}.
 *
 * <p><b>Feature:</b> safe role, status and area updates from settings.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Promotes and demotes users when another administrator remains
 *   <li>Rejects demoting or deactivating the last administrator
 *   <li>Rejects self-demotion and self-deactivation
 *   <li>Replaces assigned areas and rejects unknown area ids
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the user, area and admin ports
 *   <li>Asserts the exception on unsafe transitions and the saved role on safe ones
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class UpdateUserAccessServiceTest {

  @Mock private UserRepositoryPort userRepositoryPort;
  @Mock private AreaRepositoryPort areaRepositoryPort;
  @Mock private AdminRepositoryPort adminRepositoryPort;

  @Mock
  private aros.services.rms.core.notification.domain.port.input.NotificationUseCase
      notificationUseCase;

  private UpdateUserAccessService service;

  @BeforeEach
  void setUp() {
    service =
        new UpdateUserAccessService(
            userRepositoryPort, areaRepositoryPort, adminRepositoryPort, notificationUseCase);
  }

  @Test
  void should_promote_worker_to_admin() {
    User worker = user(10L, UserRole.WORKER, UserStatus.ACTIVE);
    when(userRepositoryPort.findById(UserId.of(10L))).thenReturn(Optional.of(worker));
    when(userRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    User result = service.changeRole(10L, UserRole.ADMIN, 1L);

    assertEquals(UserRole.ADMIN, result.getRole());
    verify(userRepositoryPort).save(worker);
  }

  @Test
  void should_reject_demoting_last_admin() {
    User admin = user(1L, UserRole.ADMIN, UserStatus.ACTIVE);
    when(userRepositoryPort.findById(UserId.of(1L))).thenReturn(Optional.of(admin));
    when(adminRepositoryPort.countByRole(UserRole.ADMIN)).thenReturn(1L);

    assertThrows(
        InvalidUserAccessException.class, () -> service.changeRole(1L, UserRole.WORKER, 2L));

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void should_reject_self_demotion() {
    User admin = user(1L, UserRole.ADMIN, UserStatus.ACTIVE);
    when(userRepositoryPort.findById(UserId.of(1L))).thenReturn(Optional.of(admin));
    when(adminRepositoryPort.countByRole(UserRole.ADMIN)).thenReturn(2L);

    assertThrows(
        InvalidUserAccessException.class, () -> service.changeRole(1L, UserRole.WORKER, 1L));

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void should_reject_deactivating_self() {
    User admin = user(1L, UserRole.ADMIN, UserStatus.ACTIVE);
    when(userRepositoryPort.findById(UserId.of(1L))).thenReturn(Optional.of(admin));

    assertThrows(
        InvalidUserAccessException.class, () -> service.changeStatus(1L, UserStatus.INACTIVE, 1L));

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void should_reject_unknown_status_values() {
    assertThrows(
        InvalidUserAccessException.class, () -> service.changeStatus(1L, UserStatus.PENDING, 2L));

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void should_reject_combining_an_exclusive_area() {
    User worker = user(10L, UserRole.WORKER, UserStatus.ACTIVE);
    when(userRepositoryPort.findById(UserId.of(10L))).thenReturn(Optional.of(worker));
    when(areaRepositoryPort.existsAllByIds(any())).thenReturn(true);
    when(areaRepositoryPort.findByIdIn(any()))
        .thenReturn(
            List.of(
                Area.builder().id(1L).name("Cocina").exclusive(true).build(),
                Area.builder().id(2L).name("Servicio").exclusive(false).build()));

    assertThrows(
        ExclusiveAreaAssignmentException.class, () -> service.changeAreas(10L, Set.of(1L, 2L)));

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void should_replace_areas_when_all_exist() {
    User worker = user(10L, UserRole.WORKER, UserStatus.ACTIVE);
    when(userRepositoryPort.findById(UserId.of(10L))).thenReturn(Optional.of(worker));
    when(areaRepositoryPort.existsAllByIds(any())).thenReturn(true);
    when(userRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    User result = service.changeAreas(10L, Set.of(2L, 3L));

    assertEquals(
        Set.of(2L, 3L), Set.copyOf(result.getAssignedAreas().stream().map(AreaId::value).toList()));
    verify(userRepositoryPort).save(worker);
  }

  @Test
  void should_reject_unknown_area() {
    User worker = user(10L, UserRole.WORKER, UserStatus.ACTIVE);
    when(userRepositoryPort.findById(UserId.of(10L))).thenReturn(Optional.of(worker));
    when(areaRepositoryPort.existsAllByIds(any())).thenReturn(false);

    assertThrows(AreaNotFoundException.class, () -> service.changeAreas(10L, Set.of(99L)));

    verify(userRepositoryPort, never()).save(any());
  }

  private static User user(Long id, UserRole role, UserStatus status) {
    return new User(
        UserId.of(id),
        "1234567890",
        "User " + id,
        new UserEmail("user" + id + "@rms.local"),
        "encoded",
        "Address",
        "3000000000",
        role,
        status,
        List.of());
  }
}
