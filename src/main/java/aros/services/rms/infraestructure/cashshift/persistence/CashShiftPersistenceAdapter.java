/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.persistence;

import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adapter for cash-shift persistence. */
@Repository
@RequiredArgsConstructor
@Transactional
public class CashShiftPersistenceAdapter implements CashShiftRepositoryPort {

  private final CashShiftJpaRepository cashShiftRepository;
  private final CashShiftEntityMapper mapper;

  /** {@inheritDoc} */
  @Override
  public CashShift save(CashShift shift) {
    if (shift.getId() == null) {
      return mapper.toDomain(cashShiftRepository.save(mapper.toEntity(shift)));
    }

    CashShiftEntity existing =
        cashShiftRepository.findById(shift.getId()).orElseGet(() -> mapper.toEntity(shift));
    existing.setStatus(shift.getStatus());
    existing.setClosedBy(shift.getClosedBy());
    existing.setClosedAt(shift.getClosedAt());
    existing.setExpectedCash(
        shift.getExpectedCash() != null ? shift.getExpectedCash().amount() : null);
    existing.setCountedCash(
        shift.getCountedCash() != null ? shift.getCountedCash().amount() : null);
    existing.setDifferenceAmount(
        shift.getDifference() != null ? shift.getDifference().amount() : null);
    existing.setNotes(shift.getNotes());
    return mapper.toDomain(cashShiftRepository.save(existing));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<CashShift> findById(Long id) {
    return cashShiftRepository.findById(id).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<CashShift> findOpen() {
    return cashShiftRepository.findFirstByStatus(CashShiftStatus.OPEN).map(mapper::toDomain);
  }
}
