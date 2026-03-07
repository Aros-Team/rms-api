package aros.services.rms.infraestructure.common.logger;

import aros.services.rms.core.common.logger.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggerImpl implements Logger {

  @Override
  public void info(String message, Object... args) {
    log.info(message, args);
  }

  @Override
  public void error(String message, Exception exception) {
    log.error(message, exception);
  }

  @Override
  public void error(String message, Exception exception, Object... args) {
    log.error(message, args, exception);
  }

  @Override
  public void debug(String message, Object... args) {
    log.debug(message, args);
  }
}
