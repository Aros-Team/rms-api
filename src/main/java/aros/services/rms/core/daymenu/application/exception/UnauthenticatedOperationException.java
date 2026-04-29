/* (C) 2026 */

package aros.services.rms.core.daymenu.application.exception;

/** Thrown when an operation requires authentication but no user is present in the context. */
public class UnauthenticatedOperationException extends RuntimeException {
  /** Creates a new exception for unauthenticated operation. */
  public UnauthenticatedOperationException() {
    super("No hay usuario autenticado para realizar esta operación");
  }
}
