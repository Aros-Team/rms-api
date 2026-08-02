/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** Adapter for PayrollRepositoryPort. */
@Repository
@RequiredArgsConstructor
@Transactional
public class PayrollPersistenceAdapter implements PayrollRepositoryPort {

  @Autowired private PayrollRepository internal;

  @Autowired private PayrollMapper payrollMapper;

  /** Saves a payroll record. */
  @Override
  public Payroll save(Payroll payroll) {
    return payrollMapper.toDomain(internal.save(payrollMapper.toEntity(payroll)));
  }

  /** Finds a payroll by ID. */
  @Override
  public Optional<Payroll> findById(Long id) {
    return internal.findById(id).map(payrollMapper::toDomain);
  }

  /** Finds a payroll by user and period. */
  @Override
  public Optional<Payroll> findByUserIdAndPeriod(Long userId, int year, int month) {
    return internal
        .findByUserIdAndPeriodYearAndPeriodMonth(userId, year, month)
        .map(payrollMapper::toDomain);
  }

  /** Returns all payroll records. */
  @Override
  public List<Payroll> findAll() {
    return internal.findAll().stream().map(payrollMapper::toDomain).toList();
  }

  /** Returns payroll records for a given period. */
  @Override
  public List<Payroll> findByPeriod(int year, int month) {
    return internal.findByPeriodYearAndPeriodMonth(year, month).stream()
        .map(payrollMapper::toDomain)
        .toList();
  }

  /** Returns payroll records for a given user. */
  @Override
  public List<Payroll> findByUserId(Long userId) {
    return internal.findByUserId(userId).stream().map(payrollMapper::toDomain).toList();
  }

  /** Returns payroll records for an area in a given period. */
  @Override
  public List<Payroll> findByAreaIdAndPeriod(Long areaId, int year, int month) {
    return internal.findByAreaIdAndPeriod(areaId, year, month).stream()
        .map(payrollMapper::toDomain)
        .toList();
  }

  /** Deletes a payroll record by ID. */
  @Override
  public void deleteById(Long id) {
    internal.deleteById(id);
  }

  /** Checks if a payroll exists for a user in a given period. */
  @Override
  public boolean existsByUserIdAndPeriod(Long userId, int year, int month) {
    return internal.existsByUserIdAndPeriodYearAndPeriodMonth(userId, year, month);
  }
}
