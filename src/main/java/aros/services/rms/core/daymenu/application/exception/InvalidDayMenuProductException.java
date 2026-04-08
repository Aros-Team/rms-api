/* (C) 2026 */
package aros.services.rms.core.daymenu.application.exception;

/** Thrown when a product without hasOptions=true is assigned as the day menu. */
public class InvalidDayMenuProductException extends RuntimeException {
  public InvalidDayMenuProductException(Long productId) {
    super(
        "El producto id="
            + productId
            + " no tiene opciones habilitadas (hasOptions=false) y no puede ser asignado como menú del día");
  }
}
