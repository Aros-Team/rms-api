package aros.services.rms.core.schedule.application.exception;

/** Thrown when a worker is not assigned to the specified shift. */
public class WorkerNotInShiftException extends RuntimeException {
  /**
   * Creates a new WorkerNotInShiftException.
   *
   * @param message the detail message
   */
  public WorkerNotInShiftException(String message) {
    super(message);
  }
}
