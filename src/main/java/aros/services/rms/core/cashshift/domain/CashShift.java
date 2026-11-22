/* (C) 2026 */

package aros.services.rms.core.cashshift.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cash shift aggregate: one drawer session from open to close.
 *
 * <p>Sales are derived from payments, never duplicated into movements. At close, {@code
 * expectedCash}, {@code countedCash} and {@code difference} are snapshotted so a Z-report never
 * changes retroactively.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashShift {
  private Long id;
  @Builder.Default private CashShiftStatus status = CashShiftStatus.OPEN;
  private Money openingFloat;
  private Money expectedCash;
  private Money countedCash;
  private Money difference;
  private Long openedBy;
  private LocalDateTime openedAt;
  private Long closedBy;
  private LocalDateTime closedAt;
  private String notes;

  /**
   * Closes the shift with the snapshotted reconciliation values.
   *
   * @param expected the expected cash computed from payments and movements
   * @param counted the cash counted by the cashier
   * @param difference {@code counted - expected}
   * @param closingNotes optional close notes
   * @param closedBy the user closing the shift
   * @param closedAt the close timestamp
   */
  public void close(
      Money expected,
      Money counted,
      Money difference,
      String closingNotes,
      Long closedBy,
      LocalDateTime closedAt) {
    this.status = CashShiftStatus.CLOSED;
    this.expectedCash = expected;
    this.countedCash = counted;
    this.difference = difference;
    this.notes = closingNotes;
    this.closedBy = closedBy;
    this.closedAt = closedAt;
  }
}
