/* (C) 2026 */

package aros.services.rms.core.common.logger;

/** Core logging interface abstracted from implementation details. */
public interface Logger {

  /**
   * Logs an info message with optional format arguments.
   *
   * @param message the message to log
   * @param args optional format arguments
   */
  void info(String message, Object... args);

  /**
   * Logs an error with exception.
   *
   * @param message the error message
   * @param exception the exception to log
   */
  void error(String message, Exception exception);

  /**
   * Logs an error with exception and additional arguments.
   *
   * @param message the error message
   * @param exception the exception to log
   * @param args optional format arguments
   */
  void error(String message, Exception exception, Object... args);

  /**
   * Logs a debug message with optional format arguments.
   *
   * @param message the message to log
   * @param args optional format arguments
   */
  void debug(String message, Object... args);
}
