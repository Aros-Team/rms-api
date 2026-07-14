/* (C) 2026 */

package aros.services.rms.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.auth.domain.AccountSetupToken;
import aros.services.rms.core.auth.port.output.AccountSetupTokenRepositoryPort;
import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.email.port.input.WelcomeEmailUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.application.service.CreateUserService;
import aros.services.rms.core.user.domain.Salary;
import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.dto.CreateUserInfo;
import aros.services.rms.core.user.port.input.CreateUserUseCase.CreateUserResult;
import aros.services.rms.core.user.port.output.SalaryHistoryRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

  @Mock private UserRepositoryPort userPort;
  @Mock private AreaRepositoryPort areaPort;
  @Mock private AccountSetupTokenRepositoryPort accountSetupTokenRepositoryPort;
  @Mock private WelcomeEmailUseCase welcomeEmailUseCase;
  @Mock private HashServicePort hashServicePort;
  @Mock private BusinessMetricsPort metricsPort;
  @Mock private SalaryHistoryRepositoryPort salaryHistoryPort;

  @Captor private ArgumentCaptor<AccountSetupToken> tokenCaptor;
  @Captor private ArgumentCaptor<SalaryHistoryEntry> salaryHistoryCaptor;

  private CreateUserService createUserService;

  private static final String DOCUMENT = "1234567890";
  private static final String NAME = "New User";
  private static final UserEmail EMAIL = new UserEmail("new@example.com");
  private static final String ADDRESS = "Address";
  private static final String PHONE = "555-0100";
  private static final Set<AreaId> AREAS = Set.of(AreaId.of(1L), AreaId.of(2L));
  private static final String TOKEN_HASH = "hashed-token";

  private CreateUserInfo validInfo;

  @BeforeEach
  void setUp() {
    createUserService =
        new CreateUserService(
            userPort,
            areaPort,
            accountSetupTokenRepositoryPort,
            welcomeEmailUseCase,
            hashServicePort,
            metricsPort,
            salaryHistoryPort);

    validInfo = new CreateUserInfo(DOCUMENT, NAME, EMAIL, ADDRESS, PHONE, AREAS, null);
  }

  private User userWithId(User user, Long id) {
    User saved =
        new User(
            UserId.of(id),
            user.getDocument(),
            user.getName(),
            user.getEmail(),
            user.getPassword(),
            user.getAddress(),
            user.getPhone(),
            user.getRole(),
            user.getStatus(),
            user.getAssignedAreas());
    saved.setSalary(user.getSalary());
    return saved;
  }

  // ---------------------------------------------------------------------------
  // UC-01: shouldCreateUserSuccessfully
  // ---------------------------------------------------------------------------

  @Test
  void shouldCreateUserSuccessfully() throws UserAlreadyExistsException {
    when(userPort.existsActiveByEmailOrDocument(DOCUMENT, EMAIL.value())).thenReturn(false);
    when(areaPort.existsAllByIds(AREAS)).thenReturn(true);
    when(userPort.save(any(User.class)))
        .thenAnswer(invocation -> userWithId(invocation.getArgument(0), 1L));
    when(hashServicePort.hash(anyString())).thenReturn(TOKEN_HASH);

    CreateUserResult result = createUserService.create(validInfo);

    assertNotNull(result);
    assertNotNull(result.user());
    assertEquals(1L, result.user().getId().value());
    assertEquals(DOCUMENT, result.user().getDocument());
    assertEquals(NAME, result.user().getName());
    assertEquals(EMAIL.value(), result.user().getEmail().value());
    assertEquals(UserRole.WORKER, result.user().getRole());
    assertEquals(UserStatus.PENDING, result.user().getStatus());

    verify(welcomeEmailUseCase)
        .sendWelcomeEmail(eq(EMAIL), anyString(), eq(NAME), eq("welcome_employee"));
    verify(accountSetupTokenRepositoryPort).save(tokenCaptor.capture());
    assertEquals(TOKEN_HASH, tokenCaptor.getValue().tokenHash());
    verify(metricsPort).recordAccountSetup("requested");
  }

  // ---------------------------------------------------------------------------
  // UC-02/03: shouldThrow_whenDocumentOrEmailAlreadyExists
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrow_whenDocumentOrEmailAlreadyExists() {
    when(userPort.existsActiveByEmailOrDocument(DOCUMENT, EMAIL.value())).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> createUserService.create(validInfo));

    verify(userPort, never()).save(any());
    verify(welcomeEmailUseCase, never())
        .sendWelcomeEmail(any(), anyString(), anyString(), anyString());
  }

  // ---------------------------------------------------------------------------
  // UC-04: shouldThrow_whenAreaNotFound
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrow_whenAreaNotFound() {
    when(userPort.existsActiveByEmailOrDocument(DOCUMENT, EMAIL.value())).thenReturn(false);
    when(areaPort.existsAllByIds(AREAS)).thenReturn(false);

    assertThrows(AreaNotFoundException.class, () -> createUserService.create(validInfo));

    verify(userPort, never()).save(any());
    verify(welcomeEmailUseCase, never())
        .sendWelcomeEmail(any(), anyString(), anyString(), anyString());
  }

  // ---------------------------------------------------------------------------
  // UC-05: shouldSetStatusError_whenEmailFails
  // ---------------------------------------------------------------------------

  @Test
  void shouldSetStatusError_whenEmailFails() throws UserAlreadyExistsException {
    when(userPort.existsActiveByEmailOrDocument(DOCUMENT, EMAIL.value())).thenReturn(false);
    when(areaPort.existsAllByIds(AREAS)).thenReturn(true);
    when(userPort.save(any(User.class)))
        .thenAnswer(invocation -> userWithId(invocation.getArgument(0), 1L));
    when(hashServicePort.hash(anyString())).thenReturn(TOKEN_HASH);

    doThrow(new RuntimeException("Email service unavailable"))
        .when(welcomeEmailUseCase)
        .sendWelcomeEmail(any(), anyString(), anyString(), anyString());

    CreateUserResult result = createUserService.create(validInfo);

    assertNotNull(result);
    assertEquals(UserStatus.ERROR, result.user().getStatus());

    verify(userPort, times(2)).save(any(User.class));
    verify(accountSetupTokenRepositoryPort, never()).save(any());
    verify(metricsPort, never()).recordAccountSetup(anyString());
  }

  // ---------------------------------------------------------------------------
  // UC-S01: shouldCreateSalaryHistoryEntry_whenUserCreatedWithSalary
  // ---------------------------------------------------------------------------

  @Test
  void shouldCreateSalaryHistoryEntry_whenUserCreatedWithSalary()
      throws UserAlreadyExistsException {
    when(userPort.existsActiveByEmailOrDocument(DOCUMENT, EMAIL.value())).thenReturn(false);
    when(areaPort.existsAllByIds(AREAS)).thenReturn(true);
    when(userPort.save(any(User.class)))
        .thenAnswer(invocation -> userWithId(invocation.getArgument(0), 1L));
    when(hashServicePort.hash(anyString())).thenReturn(TOKEN_HASH);

    Salary salary = Salary.of(new BigDecimal("2500000"));
    CreateUserInfo infoWithSalary =
        new CreateUserInfo(DOCUMENT, NAME, EMAIL, ADDRESS, PHONE, AREAS, salary);

    CreateUserResult result = createUserService.create(infoWithSalary);

    assertEquals(salary, result.user().getSalary());
    verify(salaryHistoryPort).save(salaryHistoryCaptor.capture());
    SalaryHistoryEntry entry = salaryHistoryCaptor.getValue();
    assertEquals(null, entry.getOldSalary());
    assertEquals(salary, entry.getNewSalary());
    assertEquals("CREACION", entry.getReason());
    assertEquals("Salario inicial", entry.getObservations());
  }

  // ---------------------------------------------------------------------------
  // UC-S02: shouldNotCreateSalaryHistoryEntry_whenUserCreatedWithoutSalary
  // ---------------------------------------------------------------------------

  @Test
  void shouldNotCreateSalaryHistoryEntry_whenUserCreatedWithoutSalary()
      throws UserAlreadyExistsException {
    when(userPort.existsActiveByEmailOrDocument(DOCUMENT, EMAIL.value())).thenReturn(false);
    when(areaPort.existsAllByIds(AREAS)).thenReturn(true);
    when(userPort.save(any(User.class)))
        .thenAnswer(invocation -> userWithId(invocation.getArgument(0), 1L));
    when(hashServicePort.hash(anyString())).thenReturn(TOKEN_HASH);

    createUserService.create(validInfo);

    verify(salaryHistoryPort, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // UC-S03: shouldThrow_whenSalaryIsZero
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrow_whenSalaryIsZero() {
    assertThrows(IllegalArgumentException.class, () -> Salary.of(BigDecimal.ZERO));
  }

  // ---------------------------------------------------------------------------
  // UC-S04: shouldThrow_whenSalaryIsNegative
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrow_whenSalaryIsNegative() {
    assertThrows(IllegalArgumentException.class, () -> Salary.of(new BigDecimal("-1000")));
  }
}
