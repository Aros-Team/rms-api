/* (C) 2026 */

package aros.services.rms.infraestructure.user.config;

import aros.services.rms.core.user.application.service.CreateAdminService;
import aros.services.rms.core.user.application.service.CreateAdminService.AdminConfig;
import aros.services.rms.core.user.application.service.CreateAdminService.AdminCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Initializes the admin user on application startup. */
@Component
public class AdminInitializer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

  private final CreateAdminService createAdminUseCase;

  @Value("${app.env:development}")
  private String appEnv;

  @Value("${app.admin.email:}")
  private String adminEmail;

  @Value("${app.admin.dummy-email:}")
  private String dummyEmail;

  /**
   * Creates a new AdminInitializer.
   *
   * @param createAdminUseCase the create admin use case
   */
  public AdminInitializer(CreateAdminService createAdminUseCase) {
    this.createAdminUseCase = createAdminUseCase;
  }

  /** Runs the admin initialization process. */
  @Override
  public void run(String... args) {
    try {
      boolean isProduction = "production".equalsIgnoreCase(appEnv);
      String email = isProduction ? adminEmail : dummyEmail;

      if (email == null || email.isBlank()) {
        String variable = isProduction ? "ADMIN_EMAIL" : "DUMMY_EMAIL";
        String errorMsg =
            String.format("Error: %s is required for %s environment", variable, appEnv);
        log.error(errorMsg);
        log.error("Please configure the %s variable in the .env file", variable);
        throw new IllegalStateException(errorMsg);
      }

      AdminConfig config = new AdminConfig(email, isProduction);
      AdminCredentials credentials = createAdminUseCase.execute(config);

      if (credentials == null) {
        log.info("Administrator already exists. Using stored credentials.");
        log.info("Initialization process completed");
        return;
      }

      if (credentials.isDevelopment()) {
        log.info("Initialization process completed");
      } else {
        log.info("Administrator created successfully");
        log.info("Initialization process completed");
      }

    } catch (IllegalStateException e) {
      log.error("Stopping application...");
      throw e;
    }
  }
}
