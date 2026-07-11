/* (C) 2026 */

package aros.services.rms.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.application.service.GetSalaryHistoryService;
import aros.services.rms.core.user.domain.Salary;
import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import aros.services.rms.core.user.domain.SalaryHistoryId;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.output.SalaryHistoryRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSalaryHistoryServiceTest {

  @Mock private UserRepositoryPort userPort;
  @Mock private SalaryHistoryRepositoryPort salaryHistoryPort;

  private GetSalaryHistoryService service;

  private static final Long USER_ID = 1L;

  @BeforeEach
  void setUp() {
    service = new GetSalaryHistoryService(userPort, salaryHistoryPort);
  }

  private User createUser(Long id) {
    return new User(
        UserId.of(id),
        "1234567890",
        "Test User",
        new UserEmail("test@example.com"),
        "encoded",
        "Address",
        "555-0100",
        UserRole.WORKER,
        UserStatus.ACTIVE,
        List.of());
  }

  private SalaryHistoryEntry createEntry(
      Long entryId, BigDecimal oldVal, BigDecimal newVal, String reason, String observations) {
    return new SalaryHistoryEntry(
        entryId != null ? new SalaryHistoryId(entryId) : null,
        UserId.of(USER_ID),
        oldVal != null ? Salary.of(oldVal) : null,
        Salary.of(newVal),
        Instant.now(),
        reason,
        observations);
  }

  // ---------------------------------------------------------------------------
  // UC-S12: shouldReturnSalaryHistory_whenUserExists
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturnSalaryHistory_whenUserExists() {
    when(userPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(createUser(USER_ID)));

    List<SalaryHistoryEntry> expectedEntries =
        List.of(
            createEntry(2L, new BigDecimal("2500000"), new BigDecimal("3000000"), "Aumento", null),
            createEntry(1L, null, new BigDecimal("2500000"), "CREACION", "Salario inicial"));

    when(salaryHistoryPort.findByUserId(UserId.of(USER_ID))).thenReturn(expectedEntries);

    List<SalaryHistoryEntry> result = service.getSalaryHistory(USER_ID);

    assertEquals(2, result.size());
    assertEquals(expectedEntries, result);
    verify(userPort).findById(UserId.of(USER_ID));
    verify(salaryHistoryPort).findByUserId(UserId.of(USER_ID));
  }

  // ---------------------------------------------------------------------------
  // UC-S13: shouldReturnEmptyList_whenUserHasNoHistory
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturnEmptyList_whenUserHasNoHistory() {
    when(userPort.findById(UserId.of(USER_ID))).thenReturn(Optional.of(createUser(USER_ID)));
    when(salaryHistoryPort.findByUserId(UserId.of(USER_ID))).thenReturn(List.of());

    List<SalaryHistoryEntry> result = service.getSalaryHistory(USER_ID);

    assertEquals(0, result.size());
  }

  // ---------------------------------------------------------------------------
  // UC-S14: shouldThrowUserNotFoundException_whenUserNotFound
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowUserNotFoundException_whenUserNotFound() {
    when(userPort.findById(UserId.of(USER_ID))).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.getSalaryHistory(USER_ID));
  }
}
