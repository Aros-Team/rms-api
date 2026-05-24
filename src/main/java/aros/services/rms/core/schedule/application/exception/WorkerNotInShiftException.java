package aros.services.rms.core.schedule.application.exception;

public class WorkerNotInShiftException extends RuntimeException {
  public WorkerNotInShiftException(String message) {
    super(message);
  }
}
