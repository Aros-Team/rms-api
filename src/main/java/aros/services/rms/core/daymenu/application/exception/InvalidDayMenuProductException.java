/* (C) 2026 */

package aros.services.rms.core.daymenu.application.exception;

/** Thrown when a product without hasOptions=true is assigned as the day menu. */
public class InvalidDayMenuProductException extends RuntimeException {
  /**
   * Creates a new exception for invalid day menu product.
   *
   * @param productId the product identifier that cannot be used as day menu
   */
  public InvalidDayMenuProductException(Long productId) {
    super(
        "El producto id="
            + productId
            + " no tiene opciones habilitadas (hasOptions=false) y no puede ser asignado como menú del día");
  }
}
