/* (C) 2026 */
package aros.services.rms.infraestructure.user.config;

import aros.services.rms.core.user.application.usecases.CreateAdminUseCase;
import aros.services.rms.core.user.application.usecases.CreateAdminUseCase.AdminConfig;
import aros.services.rms.core.user.application.usecases.CreateAdminUseCase.AdminCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

  private final CreateAdminUseCase createAdminUseCase;

  @Value("${app.env:development}")
  private String appEnv;

  @Value("${app.admin.email:}")
  private String adminEmail;

  @Value("${app.admin.dummy-email:}")
  private String dummyEmail;

  public AdminInitializer(CreateAdminUseCase createAdminUseCase) {
    this.createAdminUseCase = createAdminUseCase;
  }

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
        log.warn("════════════════════════════════════════════════════════════════════");
        log.warn("DEVELOPMENT MODE ACTIVE - DO NOT USE IN PRODUCTION");
        log.warn("════════════════════════════════════════════════════════════════════");

        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║           ADMINISTRATOR CREDENTIALS                       ║");
        System.out.println("║           (FOR DEVELOPMENT ONLY - DO NOT USE IN PROD)    ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("║  Email:    " + credentials.email());
        System.out.println("║  Password: " + credentials.rawPassword());
        System.out.println("╚═══════════════════════════════════════════════════════════╝");

        log.warn("Credentials printed to console. Save them, they won't be shown again.");
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
