/* (C) 2026 */

package aros.services.rms.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.application.service.UpdateUserService;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateUserServiceTest {

  @Mock private UserRepositoryPort userPort;
  @Mock private AreaRepositoryPort areaPort;

  @Captor private ArgumentCaptor<User> userCaptor;

  private UpdateUserService updateUserService;

  private static final Long USER_ID = 1L;
  private static final UserEmail ORIGINAL_EMAIL = new UserEmail("original@example.com");
  private static final UserEmail NEW_EMAIL = new UserEmail("new@example.com");
  private static final Set<AreaId> AREAS = Set.of(AreaId.of(1L), AreaId.of(2L));

  private User user;

  @BeforeEach
  void setUp() {
    updateUserService = new UpdateUserService(userPort, areaPort);

    user =
        new User(
            UserId.of(USER_ID),
            "1234567890",
            "Original Name",
            ORIGINAL_EMAIL,
            "encodedPass",
            "Original Address",
            "555-0100",
            UserRole.WORKER,
            UserStatus.ACTIVE,
            List.of(AreaId.of(1L)));
  }

  // ---------------------------------------------------------------------------
  // UC-06: shouldUpdateUserInfoSuccessfully_withoutEmailChange
  // ---------------------------------------------------------------------------

  @Test
  void shouldUpdateUserInfoSuccessfully_withoutEmailChange() {
    when(userPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
    when(areaPort.existsAllByIds(AREAS)).thenReturn(true);
    when(userPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UpdateUserInfo info =
        new UpdateUserInfo(
            "9876543210", "Updated Name", ORIGINAL_EMAIL, "New Address", "555-0200", AREAS);

    User result = updateUserService.update(USER_ID, info);

    assertEquals("9876543210", result.getDocument());
    assertEquals("Updated Name", result.getName());
    assertEquals("New Address", result.getAddress());
    assertEquals("555-0200", result.getPhone());
    assertEquals(ORIGINAL_EMAIL.value(), result.getEmail().value());

    verify(userPort).save(user);
  }

  // ---------------------------------------------------------------------------
  // UC-07: shouldChangeEmail_whenEmailDiffers
  // ---------------------------------------------------------------------------

  @Test
  void shouldChangeEmail_whenEmailDiffers() {
    when(userPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
    when(areaPort.existsAllByIds(AREAS)).thenReturn(true);
    when(userPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UpdateUserInfo info =
        new UpdateUserInfo(
            "1234567890", "Original Name", NEW_EMAIL, "Original Address", "555-0100", AREAS);

    User result = updateUserService.update(USER_ID, info);

    assertEquals(NEW_EMAIL.value(), result.getEmail().value());
    verify(userPort).save(user);
  }

  // ---------------------------------------------------------------------------
  // UC-08: shouldThrow_whenUserNotFound
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrow_whenUserNotFound() {
    when(userPort.findById(UserId.of(USER_ID))).thenReturn(Optional.empty());

    UpdateUserInfo info =
        new UpdateUserInfo("1234567890", "Name", ORIGINAL_EMAIL, "Address", "555-0100", AREAS);

    assertThrows(UserNotFoundException.class, () -> updateUserService.update(USER_ID, info));

    verify(userPort, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-09: shouldThrow_whenAreaNotFound
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrow_whenAreaNotFound() {
    when(userPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
    when(areaPort.existsAllByIds(anySet())).thenReturn(false);

    UpdateUserInfo info =
        new UpdateUserInfo("1234567890", "Name", ORIGINAL_EMAIL, "Address", "555-0100", AREAS);

    assertThrows(AreaNotFoundException.class, () -> updateUserService.update(USER_ID, info));

    verify(userPort, never()).save(any());
  }
}
