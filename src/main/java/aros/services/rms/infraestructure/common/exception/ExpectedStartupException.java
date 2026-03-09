/* (C) 2026 */
package aros.services.rms.infraestructure.common.exception;

public class ExpectedStartupException extends RuntimeException {

  private final String userMessage;

  public ExpectedStartupException(String userMessage) {
    super(userMessage);
    this.userMessage = userMessage;
  }

  public String getUserMessage() {
    return userMessage;
  }
}
