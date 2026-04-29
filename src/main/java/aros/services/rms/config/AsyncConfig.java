package aros.services.rms.config;

import java.util.concurrent.Executor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * Configuration for asynchronous task execution using virtual threads.
 *
 * <p>This config provides a virtual thread-based executor for async operations and custom exception
 * handling for uncaught async exceptions.
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

  /**
   * Creates a virtual thread task executor for async operations.
   *
   * @return a new VirtualThreadTaskExecutor with prefix "audit-"
   */
  @Bean(name = "virtualThreadExecutor")
  public VirtualThreadTaskExecutor virtualThreadTaskExecutor() {
    return new VirtualThreadTaskExecutor("audit-");
  }

  @Override
  public Executor getAsyncExecutor() {
    return virtualThreadTaskExecutor();
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (throwable, method, args) -> {};
  }
}
