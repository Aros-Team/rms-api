/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.email.port.input.RegistrationEmailUseCase;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.output.AdminRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service responsible for creating the initial administrator account. */
@Service
public class CreateAdminService {

  private static final Logger log = LoggerFactory.getLogger(CreateAdminService.class);

  private final UserRepositoryPort userRepository;
  private final AdminRepositoryPort adminRepository;
  private final PasswordEncoderPort passwordEncoder;
  private final RegistrationEmailUseCase emailService;

  /** Configuration for admin creation with email and environment flag. */
  public record AdminConfig(String email, boolean isProduction) {}

  /** Credentials returned after admin creation with email, raw password, and environment flag. */
  public record AdminCredentials(String email, String rawPassword, boolean isDevelopment) {}

  /**
   * Creates the admin service.
   *
   * @param userRepository repository for user operations
   * @param adminRepository repository for admin operations
   * @param passwordEncoder port for password encoding
   * @param registrationEmailUseCase use case for sending registration emails
   */
  public CreateAdminService(
      UserRepositoryPort userRepository,
      AdminRepositoryPort adminRepository,
      PasswordEncoderPort passwordEncoder,
      RegistrationEmailUseCase registrationEmailUseCase) {
    this.userRepository = userRepository;
    this.adminRepository = adminRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailService = registrationEmailUseCase;
  }

  /**
   * Executes admin creation based on configuration.
   *
   * @param config configuration for admin creation
   * @return credentials for the created admin
   */
  @Transactional
  public AdminCredentials execute(AdminConfig config) {
    log.info("Starting administrator verification...");

    if (config.isProduction()) {
      log.info("Operation mode: production");
      return createProductionAdmin(config.email());
    } else {
      log.info("Operation mode: development");
      return createDevelopmentAdmin(config.email());
    }
  }

  private AdminCredentials createProductionAdmin(String adminEmail) {
    log.debug("Administrator email: {}", adminEmail);

    long adminCount = adminRepository.countByRole(UserRole.ADMIN);
    if (adminCount > 0) {
      log.warn(
          "Administrator creation not required. "
              + "An ADMIN role user already exists in the database.");
      return null;
    }

    log.info("No administrator found, creating new one...");

    String rawPassword = GenerateSecurePasswordService.execute();
    String hashedPassword = passwordEncoder.encode(rawPassword);

    User admin =
        new User(
            null,
            "00000000",
            "Administrator",
            new UserEmail(adminEmail),
            hashedPassword,
            "",
            "",
            UserRole.ADMIN,
            UserStatus.ACTIVE,
            List.of());

    userRepository.save(admin);
    log.info("Administrator created successfully");

    sendEmail(adminEmail, rawPassword, false);

    return new AdminCredentials(adminEmail, rawPassword, false);
  }

  private AdminCredentials createDevelopmentAdmin(String dummyEmail) {
    log.debug("Administrator email: {}", dummyEmail);

    long adminCount = adminRepository.countByRole(UserRole.ADMIN);

    if (adminCount > 0) {
      log.warn("Administrator already exists in database, skipping creation.");
      return null;
    }

    log.info("No administrator found, creating new one...");

    String rawPassword = GenerateSecurePasswordService.execute();
    String hashedPassword = passwordEncoder.encode(rawPassword);

    User admin =
        new User(
            null,
            "00000000",
            "Administrator",
            new UserEmail(dummyEmail),
            hashedPassword,
            "",
            "",
            UserRole.ADMIN,
            UserStatus.ACTIVE,
            List.of());

    userRepository.save(admin);
    log.info("Administrator created successfully and persisted in database");

    boolean isFirstTime = adminCount == 0;
    if (isFirstTime) {
      sendEmail(dummyEmail, rawPassword, true);
    }

    return new AdminCredentials(dummyEmail, rawPassword, isFirstTime);
  }

  private void sendEmail(String email, String password, boolean isFirstTime) {
    log.info("Sending email with credentials...");

    try {
      String message;
      if (isFirstTime) {
        message =
            String.format(
                "\n=============================================%n%n"
                    + "Administrator credentials (DEVELOPMENT):%n%n"
                    + "Email: %s%n"
                    + "Password: %s%n%n"
                    + "=============================================%n%n"
                    + "Note: Save these credentials. They will not be shown again.%n"
                    + "DO NOT use in production.\n",
                email, password);
      } else {
        message =
            String.format(
                "Welcome to the RMS system.%n%n"
                    + "Your administrator credentials are:%n%n"
                    + "Email: %s%n"
                    + "Password: %s%n%n"
                    + "Please change your password after the first login.",
                email, password);
      }

      emailService.sendRegistrationMail(new UserEmail(email), message);
    } catch (Exception e) {
      if (isFirstTime) {
        log.warn("Email could not be sent (development mode - continuing): {}", e.getMessage());
      } else {
        log.error("Error sending email: {}", e.getMessage());
        throw new IllegalStateException("Error sending email", e);
      }
    }
  }
}
