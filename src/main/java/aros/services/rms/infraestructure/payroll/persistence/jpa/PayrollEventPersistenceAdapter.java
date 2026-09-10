/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.port.output.PayrollEventRepositoryPort;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adapter for PayrollEventRepositoryPort. */
@Repository
@RequiredArgsConstructor
@Transactional
public class PayrollEventPersistenceAdapter implements PayrollEventRepositoryPort {

  private final PayrollEventRepository repository;
  private final PayrollEventMapper mapper;

  /** Saves a payroll event. */
  @Override
  public PayrollEvent save(PayrollEvent event) {
    return mapper.toDomain(repository.save(mapper.toEntity(event)));
  }

  /** Finds events for a user within a date range. */
  @Override
  public List<PayrollEvent> findByUserAndPeriod(
      Long userId, LocalDate startDate, LocalDate endDate) {
    return repository.findByUserAndPeriod(userId, startDate, endDate).stream()
        .map(mapper::toDomain)
        .toList();
  }

  /** Finds a payroll event by ID. */
  @Override
  public Optional<PayrollEvent> findById(Long id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  /** Deletes a payroll event by ID. */
  @Override
  public void deleteById(Long id) {
    repository.deleteById(id);
  }

  /**
   * Finds all events associated with a payroll record. Since payroll_events has no payroll_id
   * column, this returns an empty list. Events are linked to payrolls through user and period
   * matching at the application layer.
   *
   * @param payrollId the payroll id
   * @return empty list (no direct link between events and payrolls in the schema)
   */
  @Override
  public List<PayrollEvent> findByPayrollId(Long payrollId) {
    return Collections.emptyList();
  }
}
