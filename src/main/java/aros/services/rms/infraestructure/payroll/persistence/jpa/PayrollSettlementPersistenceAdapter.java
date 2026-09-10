/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adapter for PayrollSettlementRepositoryPort. */
@Repository
@RequiredArgsConstructor
@Transactional
public class PayrollSettlementPersistenceAdapter implements PayrollSettlementRepositoryPort {

  private final PayrollSettlementRepository repository;
  private final PayrollSettlementMapper mapper;

  /** Saves a payroll settlement. */
  @Override
  public PayrollSettlement save(PayrollSettlement settlement) {
    return mapper.toDomain(repository.save(mapper.toEntity(settlement)));
  }

  /** Finds settlements for a user within a date range. */
  @Override
  public List<PayrollSettlement> findByUserAndPeriod(
      Long userId, LocalDate startDate, LocalDate endDate) {
    return repository.findByUserIdAndPeriodStartBetween(userId, startDate, endDate).stream()
        .map(mapper::toDomain)
        .toList();
  }

  /** Finds all settlements for a given payroll record. */
  @Override
  public List<PayrollSettlement> findByPayrollId(Long payrollId) {
    return repository.findByPayrollId(payrollId).stream().map(mapper::toDomain).toList();
  }
}
