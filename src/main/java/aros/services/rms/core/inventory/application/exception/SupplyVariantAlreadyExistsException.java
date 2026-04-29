/* (C) 2026 */

package aros.services.rms.core.inventory.application.exception;

/** Thrown when a supply variant with the same supply, unit and quantity already exists. */
public class SupplyVariantAlreadyExistsException extends RuntimeException {

  /**
   * Creates exception by supply, unit and quantity.
   *
   * @param supplyId supply ID
   * @param unitId unit ID
   * @param quantity the quantity
   */
  public SupplyVariantAlreadyExistsException(Long supplyId, Long unitId, String quantity) {
    super(
        "Supply variant already exists: supplyId="
            + supplyId
            + ", unitId="
            + unitId
            + ", quantity="
            + quantity);
  }
}
