/* (C) 2026 */

package aros.services.rms.core.analytics.domain.exception;

/** Raised when an alert id does not exist. */
public class AlertNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception for the supplied alert id.
   *
   * @param id the missing alert id
   */
  public AlertNotFoundException(Long id) {
    super("Alerta no encontrada: " + id);
  }
}
