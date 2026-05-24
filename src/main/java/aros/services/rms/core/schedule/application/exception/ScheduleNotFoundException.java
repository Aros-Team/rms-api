package aros.services.rms.core.schedule.application.exception;

public class ScheduleNotFoundException extends RuntimeException {
  public ScheduleNotFoundException(String message) {
    super(message);
  }
}
