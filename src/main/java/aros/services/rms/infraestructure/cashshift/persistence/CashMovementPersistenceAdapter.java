/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.persistence;

import aros.services.rms.core.cashshift.domain.CashMovement;
import aros.services.rms.core.cashshift.port.output.CashMovementRepositoryPort;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adapter for the cash-movement ledger (read side). */
@Repository
@RequiredArgsConstructor
@Transactional
public class CashMovementPersistenceAdapter implements CashMovementRepositoryPort {

  private final CashMovementJpaRepository cashMovementRepository;
  private final CashMovementEntityMapper mapper;
  private final CurrencyProvider currencyProvider;

  /** {@inheritDoc} */
  @Override
  public List<CashMovement> findByShiftId(Long shiftId) {
    var currency = currencyProvider.getCurrency();
    return cashMovementRepository.findByShiftId(shiftId).stream()
        .map(entity -> mapper.toDomain(entity, currency))
        .toList();
  }
}
