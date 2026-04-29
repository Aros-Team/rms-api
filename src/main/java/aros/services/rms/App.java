/* (C) 2026 */

package aros.services.rms;

import aros.services.rms.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Root application class for the RMS (Restaurant Management System) service.
 *
 * <p>Bootstraps the Spring Boot application with async support and configuration properties
 * enabled.
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableAsync
public class App {

  /**
   * Main entry point for the RMS application.
   *
   * @param args command line arguments passed to the application
   */
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
