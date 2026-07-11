/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.input.GetSalaryHistoryUseCase;
import aros.services.rms.core.user.port.output.SalaryHistoryRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.List;

/** Implementation of salary history retrieval use case. */
public class GetSalaryHistoryService implements GetSalaryHistoryUseCase {

  private final UserRepositoryPort userPort;
  private final SalaryHistoryRepositoryPort salaryHistoryPort;

  /**
   * Creates a service to retrieve salary history.
   *
   * @param userPort repository for user operations
   * @param salaryHistoryPort repository for salary history operations
   */
  public GetSalaryHistoryService(
      UserRepositoryPort userPort, SalaryHistoryRepositoryPort salaryHistoryPort) {
    this.userPort = userPort;
    this.salaryHistoryPort = salaryHistoryPort;
  }

  @Override
  public List<SalaryHistoryEntry> getSalaryHistory(Long userId) {
    this.userPort
        .findById(UserId.of(userId))
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    return this.salaryHistoryPort.findByUserId(UserId.of(userId));
  }
}
