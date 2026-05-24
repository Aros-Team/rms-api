package aros.services.rms.core.schedule.application.exception;

public class ShiftOverlapException extends RuntimeException {
  private final String details;

  public ShiftOverlapException(String message) {
    super(message);
    this.details = message;
  }

  public ShiftOverlapException(String message, String details) {
    super(message);
    this.details = details;
  }

  public String getDetails() {
    return details;
  }
}
