/* (C) 2026 */

package aros.services.rms.core.cashshift.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Manual cash movement (petty cash in or out).
 *
 * @param id persistence identifier, {@code null} before saving
 * @param shiftId the owning shift
 * @param type the movement direction
 * @param amount the positive movement amount
 * @param reason the mandatory justification
 * @param createdBy the user who registered it
 * @param createdAt the registration timestamp
 */
public record CashMovement(
    Long id,
    Long shiftId,
    CashMovementType type,
    Money amount,
    String reason,
    Long createdBy,
    LocalDateTime createdAt) {

  /**
   * Canonical constructor with validation.
   *
   * @param id persistence identifier
   * @param shiftId the owning shift
   * @param type the movement direction
   * @param amount the positive amount
   * @param reason the justification
   * @param createdBy the registering user
   * @param createdAt the timestamp
   */
  public CashMovement {
    Objects.requireNonNull(shiftId, "shiftId");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(amount, "amount");
  }
}
