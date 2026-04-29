/* (C) 2026 */
package aros.services.rms.infraestructure.common.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Validator for JWT configuration. */
@Component
public class JwtConfigValidator {

  private static final Logger log = LoggerFactory.getLogger(JwtConfigValidator.class);
  private static final String PRODUCTION = "production";
  private static final String ERROR_MESSAGE =
      "JWT keys not configured. Run './gradlew generate-jwt-keys' or 'task jwtkeys' to generate and add to .env file";
  private static final String PRODUCTION_ERROR_MESSAGE =
      "CRITICAL: JWT keys are required in production. Application cannot start without JWT configuration.";

  private final String publicKey;
  private final String privateKey;
  private final String appEnv;

  /**
   * Creates a new JwtConfigValidator.
   *
   * @param publicKey the JWT public key
   * @param privateKey the JWT private key
   * @param appEnv the application environment
   */
  public JwtConfigValidator(
      @Value("${app.jwt.public-key:}") String publicKey,
      @Value("${app.jwt.private-key:}") String privateKey,
      @Value("${app.env:development}") String appEnv) {
    this.publicKey = normalizeKey(publicKey);
    this.privateKey = normalizeKey(privateKey);
    this.appEnv = appEnv;
  }

  /** Validates the JWT configuration. */
  @PostConstruct
  public void validate() {
    boolean isProduction = PRODUCTION.equals(appEnv);
    boolean isConfigured = isConfigured();

    if (!isConfigured) {
      if (isProduction) {
        log.error(PRODUCTION_ERROR_MESSAGE);
        throw new IllegalStateException(PRODUCTION_ERROR_MESSAGE);
      } else {
        log.warn("JWT keys not configured. Running in development mode with limited security.");
      }
    } else {
      log.info("JWT configuration validated successfully");
    }
  }

  public String getPublicKey() {
    return publicKey;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public boolean isConfigured() {
    return publicKey != null && !publicKey.isBlank() && privateKey != null && !privateKey.isBlank();
  }

  /** Normalizes the key by replacing escaped newlines. */
  private String normalizeKey(String key) {
    if (key == null || key.isBlank()) return key;
    key = key.trim();
    // Unescape \n literal sequences (from .env file) to actual newlines
    key = key.replace("\\n", "\n");
    // Remove Windows carriage returns if present
    key = key.replace("\r", "");
    return key;
  }
}
