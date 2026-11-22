/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.persistence;

import aros.services.rms.core.cashshift.domain.CashMovement;
import aros.services.rms.core.common.money.domain.Money;
import java.util.Currency;
import org.springframework.stereotype.Component;

/** Maps between the cash-movement domain record and its JPA entity. */
@Component
public class CashMovementEntityMapper {

  /**
   * Converts an entity to a domain movement.
   *
   * @param entity the entity
   * @param currency the movement currency
   * @return the domain movement
   */
  public CashMovement toDomain(CashMovementEntity entity, Currency currency) {
    if (entity == null) {
      return null;
    }
    return new CashMovement(
        entity.getId(),
        entity.getShiftId(),
        entity.getType(),
        new Money(entity.getAmount(), currency),
        entity.getReason(),
        entity.getCreatedBy(),
        entity.getCreatedAt());
  }
}
