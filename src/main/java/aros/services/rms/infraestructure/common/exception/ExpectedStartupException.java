/* (C) 2026 */
package aros.services.rms.infraestructure.common.exception;

/** Base exception for expected startup failures. */
public class ExpectedStartupException extends RuntimeException {

  private final String userMessage;

  /** Creates a new exception. */
  public ExpectedStartupException(String userMessage) {
    super(userMessage);
    this.userMessage = userMessage;
  }

  public String getUserMessage() {
    return userMessage;
  }
}
