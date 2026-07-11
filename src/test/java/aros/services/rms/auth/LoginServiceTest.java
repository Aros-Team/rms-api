/* (C) 2026 */

package aros.services.rms.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.Credentials;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.application.service.LoginService;
import aros.services.rms.core.auth.domain.RefreshToken;
import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.auth.port.output.RefreshTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.TokenPort;
import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.device.domain.Device;
import aros.services.rms.core.device.domain.DeviceId;
import aros.services.rms.core.device.port.output.DeviceRepositoryPort;
import aros.services.rms.core.email.port.input.TwoFactorAuthEmailUseCase;
import aros.services.rms.core.schedule.port.input.RecordTimeLogUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.twofactor.domain.TwoFactorCode;
import aros.services.rms.core.twofactor.port.output.TfaCodeGeneratorPort;
import aros.services.rms.core.twofactor.port.output.TwoFactorCodeRepositoryPort;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  @Mock private PasswordEncoderPort passwordPort;
  @Mock private UserRepositoryPort userPort;
  @Mock private DeviceRepositoryPort devicePort;
  @Mock private TfaCodeGeneratorPort tfaCodeGeneratorPort;
  @Mock private TwoFactorCodeRepositoryPort tfaPort;
  @Mock private TwoFactorAuthEmailUseCase emailPort;
  @Mock private HashServicePort hashServicePort;
  @Mock private RefreshTokenRepositoryPort refreshTokenPort;
  @Mock private TokenPort tokenPort;
  @Mock private BusinessMetricsPort metricsPort;
  @Mock private RecordTimeLogUseCase recordTimeLogUseCase;

  private LoginService loginService;

  private static final UserEmail EMAIL = new UserEmail("test@example.com");
  private static final UserId USER_ID = UserId.of(1L);
  private static final String RAW_PASS = "rawPass";
  private static final String ENCODED_PASS = "encodedPass";
  private static final String KNOWN_DEVICE_HASH = "known-device";
  private static final String UNKNOWN_DEVICE_HASH = "unknown-device";
  private static final String ACCESS_TOKEN = "access-token";
  private static final String REFRESH_TOKEN = "refresh-token";
  private static final String TFA_TOKEN = "tfa-token";
  private static final String HASHED_REFRESH = "hashed-refresh";
  private static final String TFA_CODE = "123456";
  private static final String HASHED_TFA = "hashed-123456";

  private User user;
  private Device device;

  @BeforeEach
  void setUp() {
    user =
        new User(
            USER_ID,
            "12345678",
            "Test User",
            EMAIL,
            ENCODED_PASS,
            "Address",
            "555-0100",
            UserRole.WORKER,
            UserStatus.ACTIVE,
            List.of());

    device = new Device(new DeviceId(1L), USER_ID, KNOWN_DEVICE_HASH);

    loginService =
        new LoginService(
            passwordPort,
            userPort,
            devicePort,
            tfaCodeGeneratorPort,
            tfaPort,
            emailPort,
            hashServicePort,
            refreshTokenPort,
            tokenPort,
            metricsPort,
            recordTimeLogUseCase);
  }

  // ---------------------------------------------------------------------------
  // UC-01: shouldReturnSuccess_whenCredentialsValidAndDeviceKnown
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturnSuccess_whenCredentialsValidAndDeviceKnown() {
    when(userPort.findByEmail(EMAIL.value())).thenReturn(Optional.of(user));
    when(passwordPort.validate(RAW_PASS, ENCODED_PASS)).thenReturn(true);
    when(devicePort.findByUserIdAndHash(USER_ID, KNOWN_DEVICE_HASH))
        .thenReturn(Optional.of(device));
    when(recordTimeLogUseCase.execute(any()))
        .thenReturn(new RecordTimeLogUseCase.RecordTimeLogResult(false, null));
    when(tokenPort.generateAccessToken(user, true)).thenReturn(ACCESS_TOKEN);
    when(tokenPort.generateRefreshToken(user)).thenReturn(REFRESH_TOKEN);
    when(hashServicePort.hash(REFRESH_TOKEN)).thenReturn(HASHED_REFRESH);

    Credentials credentials = new Credentials(EMAIL, RAW_PASS, KNOWN_DEVICE_HASH);
    AuthResult result = loginService.authenticate(credentials);

    assertInstanceOf(AuthResult.Success.class, result);
    AuthResult.Success success = (AuthResult.Success) result;
    assertEquals(EMAIL.value(), success.username());
    assertEquals(ACCESS_TOKEN, success.acessToken());
    assertEquals(REFRESH_TOKEN, success.refreshToken());

    ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenPort, times(1)).save(tokenCaptor.capture());
    assertEquals(USER_ID, tokenCaptor.getValue().getUserId());
    assertEquals(HASHED_REFRESH, tokenCaptor.getValue().getTokenHash());

    verify(metricsPort, times(1)).recordLoginAttempt("success");
    verify(metricsPort, never()).recordLoginAttempt("failure");
    verify(metricsPort, never()).recordLoginAttempt("tfa_required");
  }

  // ---------------------------------------------------------------------------
  // UC-02: shouldReturnRequiresTfa_whenCredentialsValidAndDeviceUnknown
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturnRequiresTfa_whenCredentialsValidAndDeviceUnknown() {
    when(userPort.findByEmail(EMAIL.value())).thenReturn(Optional.of(user));
    when(passwordPort.validate(RAW_PASS, ENCODED_PASS)).thenReturn(true);
    when(devicePort.findByUserIdAndHash(USER_ID, UNKNOWN_DEVICE_HASH)).thenReturn(Optional.empty());
    when(tfaCodeGeneratorPort.generateCode(6)).thenReturn(TFA_CODE);
    when(hashServicePort.hash(TFA_CODE)).thenReturn(HASHED_TFA);
    when(tokenPort.generateTfaToken(user)).thenReturn(TFA_TOKEN);

    Credentials credentials = new Credentials(EMAIL, RAW_PASS, UNKNOWN_DEVICE_HASH);
    AuthResult result = loginService.authenticate(credentials);

    assertInstanceOf(AuthResult.RequiresTfa.class, result);
    AuthResult.RequiresTfa tfaResult = (AuthResult.RequiresTfa) result;
    assertEquals(EMAIL.value(), tfaResult.username());
    assertEquals(TFA_TOKEN, tfaResult.acessToken());

    ArgumentCaptor<TwoFactorCode> codeCaptor = ArgumentCaptor.forClass(TwoFactorCode.class);
    verify(tfaPort, times(1)).save(codeCaptor.capture());
    assertEquals(USER_ID, codeCaptor.getValue().userId());
    assertEquals(HASHED_TFA, codeCaptor.getValue().codeHash());

    verify(emailPort, times(1)).sendTwoFactorCode(EMAIL, TFA_CODE);
    verify(metricsPort, times(1)).recordLoginAttempt("tfa_required");
    verify(refreshTokenPort, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-03: shouldThrowInvalidCredentials_whenUserNotFound
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowInvalidCredentials_whenUserNotFound() {
    when(userPort.findByEmail(EMAIL.value())).thenReturn(Optional.empty());

    Credentials credentials = new Credentials(EMAIL, RAW_PASS, KNOWN_DEVICE_HASH);

    assertThrows(InvalidCredentialsException.class, () -> loginService.authenticate(credentials));

    verify(userPort, times(1)).findByEmail(EMAIL.value());
    verify(metricsPort, times(1)).recordLoginAttempt("failure");
    verifyNoInteractions(passwordPort, devicePort, tokenPort);
  }

  // ---------------------------------------------------------------------------
  // UC-04: shouldThrowInvalidCredentials_whenPasswordMismatch
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowInvalidCredentials_whenPasswordMismatch() {
    when(userPort.findByEmail(EMAIL.value())).thenReturn(Optional.of(user));
    when(passwordPort.validate(RAW_PASS, ENCODED_PASS)).thenReturn(false);

    Credentials credentials = new Credentials(EMAIL, RAW_PASS, KNOWN_DEVICE_HASH);

    assertThrows(InvalidCredentialsException.class, () -> loginService.authenticate(credentials));

    verify(passwordPort, times(1)).validate(RAW_PASS, ENCODED_PASS);
    verify(metricsPort, times(1)).recordLoginAttempt("failure");
    verify(devicePort, never()).findByUserIdAndHash(any(), any());
  }
}
