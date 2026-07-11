/* (C) 2026 */

package aros.services.rms.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.user.application.exception.InvalidPasswordException;
import aros.services.rms.core.user.application.exception.SamePasswordException;
import aros.services.rms.core.user.application.exception.UserNotFoundByEmailException;
import aros.services.rms.core.user.application.service.ChangePasswordService;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

  @Mock private UserRepositoryPort userRepositoryPort;
  @Mock private PasswordEncoderPort passwordEncoderPort;

  private ChangePasswordService changePasswordService;

  private static final String EMAIL = "user@example.com";
  private static final String CURRENT_PASSWORD = "CurrentPass1!";
  private static final String NEW_PASSWORD_VALID = "NewPass123!";
  private static final String NEW_PASSWORD_WEAK = "weak";
  private static final String ENCODED_PASSWORD = "$2a$10$encoded";

  private User user;

  @BeforeEach
  void setUp() {
    changePasswordService = new ChangePasswordService(userRepositoryPort, passwordEncoderPort);

    user =
        new User(
            UserId.of(1L),
            "1234567890",
            "Test User",
            new UserEmail(EMAIL),
            ENCODED_PASSWORD,
            "Address",
            "555-0100",
            UserRole.WORKER,
            UserStatus.ACTIVE,
            List.of());
  }

  // ---------------------------------------------------------------------------
  // UC-10: shouldChangePasswordSuccessfully
  // ---------------------------------------------------------------------------

  @Test
  void shouldChangePasswordSuccessfully() {
    when(userRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoderPort.validate(CURRENT_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
    when(passwordEncoderPort.validate(NEW_PASSWORD_VALID, ENCODED_PASSWORD)).thenReturn(false);
    when(passwordEncoderPort.encode(NEW_PASSWORD_VALID)).thenReturn("newEncoded");

    changePasswordService.changePassword(EMAIL, CURRENT_PASSWORD, NEW_PASSWORD_VALID);

    verify(userRepositoryPort).save(user);
  }

  // ---------------------------------------------------------------------------
  // UC-11: shouldThrow_whenEmailNotFound
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrow_whenEmailNotFound() {
    when(userRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundByEmailException.class,
        () -> changePasswordService.changePassword(EMAIL, CURRENT_PASSWORD, NEW_PASSWORD_VALID));

    verify(userRepositoryPort, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-12: shouldThrow_whenCurrentPasswordWrong
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrow_whenCurrentPasswordWrong() {
    when(userRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoderPort.validate(CURRENT_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

    assertThrows(
        InvalidPasswordException.class,
        () -> changePasswordService.changePassword(EMAIL, CURRENT_PASSWORD, NEW_PASSWORD_VALID));

    verify(userRepositoryPort, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-13: shouldThrow_whenNewPasswordSameAsCurrent
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrow_whenNewPasswordSameAsCurrent() {
    when(userRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoderPort.validate(CURRENT_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
    when(passwordEncoderPort.validate(NEW_PASSWORD_VALID, ENCODED_PASSWORD)).thenReturn(true);

    assertThrows(
        SamePasswordException.class,
        () -> changePasswordService.changePassword(EMAIL, CURRENT_PASSWORD, NEW_PASSWORD_VALID));

    verify(userRepositoryPort, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-14: shouldThrow_whenNewPasswordDoesNotMeetPolicy
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrow_whenNewPasswordDoesNotMeetPolicy() {
    when(userRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoderPort.validate(CURRENT_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
    when(passwordEncoderPort.validate(NEW_PASSWORD_WEAK, ENCODED_PASSWORD)).thenReturn(false);

    assertThrows(
        InvalidPasswordException.class,
        () -> changePasswordService.changePassword(EMAIL, CURRENT_PASSWORD, NEW_PASSWORD_WEAK));

    verify(userRepositoryPort, never()).save(any());
  }
}
