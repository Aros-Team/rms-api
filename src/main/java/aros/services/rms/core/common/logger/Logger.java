package aros.services.rms.core.common.logger;

public interface Logger {

  void info(String message, Object... args);

  void error(String message, Exception exception);

  void error(String message, Exception exception, Object... args);

  void debug(String message, Object... args);
}
