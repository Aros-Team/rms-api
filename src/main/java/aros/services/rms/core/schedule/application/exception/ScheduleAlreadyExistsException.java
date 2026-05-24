package aros.services.rms.core.schedule.application.exception;

public class ScheduleAlreadyExistsException extends RuntimeException {
  public ScheduleAlreadyExistsException(String message) {
    super(message);
  }
}
