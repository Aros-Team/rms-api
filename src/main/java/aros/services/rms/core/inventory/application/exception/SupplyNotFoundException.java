/* (C) 2026 */

package aros.services.rms.core.inventory.application.exception;

/** Exception thrown when a supply is not found. */
public class SupplyNotFoundException extends RuntimeException {

  /**
   * Creates exception by ID.
   *
   * @param id supply ID
   */
  public SupplyNotFoundException(Long id) {
    super("Supply not found: " + id);
  }
}
