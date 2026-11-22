/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.persistence;

import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.springframework.stereotype.Component;

/** Maps between the cash-shift domain aggregate and its JPA entity. */
@Component
public class CashShiftEntityMapper {

  /**
   * Converts a domain shift to an entity.
   *
   * @param shift the domain shift
   * @return the entity
   */
  public CashShiftEntity toEntity(CashShift shift) {
    if (shift == null) {
      return null;
    }
    return CashShiftEntity.builder()
        .id(shift.getId())
        .status(shift.getStatus())
        .currency(shift.getOpeningFloat().currency().getCurrencyCode())
        .openedBy(shift.getOpenedBy())
        .openedAt(shift.getOpenedAt())
        .closedBy(shift.getClosedBy())
        .closedAt(shift.getClosedAt())
        .openingFloat(shift.getOpeningFloat().amount())
        .expectedCash(amount(shift.getExpectedCash()))
        .countedCash(amount(shift.getCountedCash()))
        .differenceAmount(amount(shift.getDifference()))
        .notes(shift.getNotes())
        .build();
  }

  /**
   * Converts an entity to a domain shift.
   *
   * @param entity the entity
   * @return the domain shift
   */
  public CashShift toDomain(CashShiftEntity entity) {
    if (entity == null) {
      return null;
    }
    Currency currency = Currency.getInstance(entity.getCurrency());
    return CashShift.builder()
        .id(entity.getId())
        .status(entity.getStatus())
        .openingFloat(new Money(entity.getOpeningFloat(), currency))
        .expectedCash(money(entity.getExpectedCash(), currency))
        .countedCash(money(entity.getCountedCash(), currency))
        .difference(money(entity.getDifferenceAmount(), currency))
        .openedBy(entity.getOpenedBy())
        .openedAt(entity.getOpenedAt())
        .closedBy(entity.getClosedBy())
        .closedAt(entity.getClosedAt())
        .notes(entity.getNotes())
        .build();
  }

  private static BigDecimal amount(Money money) {
    return money != null ? money.amount() : null;
  }

  private static Money money(BigDecimal amount, Currency currency) {
    return amount != null ? new Money(amount, currency) : null;
  }
}
