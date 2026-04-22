/* (C) 2026 */
package aros.services.rms.infraestructure.common.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfigValidator {

  private static final Logger log = LoggerFactory.getLogger(JwtConfigValidator.class);
  private static final String PRODUCTION = "production";
  private static final String ERROR_MESSAGE =
      "JWT keys not configured. Run './gradlew generate-jwt-keys' or 'task jwtkeys' to generate and add to .env file";
  private static final String PRODUCTION_ERROR_MESSAGE =
      "CRITICAL: JWT keys are required in production. Application cannot start without JWT configuration.";
  private static final String PEM_BEGIN_PRIVATE = "-----BEGIN RSA PRIVATE KEY-----";
  private static final String PEM_END_PRIVATE = "-----END RSA PRIVATE KEY-----";
  private static final String PEM_BEGIN_PUBLIC = "-----BEGIN RSA PUBLIC KEY-----";
  private static final String PEM_END_PUBLIC = "-----END RSA PUBLIC KEY-----";
  private static final String PEM_BEGIN_RSA_PUBLIC = "-----BEGIN PUBLIC KEY-----";
  private static final String PEM_END_RSA_PUBLIC = "-----END PUBLIC KEY-----";

  private final String publicKey;
  private final String privateKey;
  private final String appEnv;

  public JwtConfigValidator(
      @Value("${app.jwt.public-key:}") String publicKey,
      @Value("${app.jwt.private-key:}") String privateKey,
      @Value("${app.env:development}") String appEnv) {
    this.publicKey = normalizeKey(publicKey);
    this.privateKey = normalizeKey(privateKey);
    this.appEnv = appEnv;
  }

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

  private String normalizeKey(String key) {
    if (key == null || key.isBlank()) return key;
    key = key.trim();
    if (!key.startsWith("-----BEGIN")) return key;

    // Remove PEM headers and newlines to get raw base64
    String normalized = key;
    normalized = stripPemWrapper(normalized, PEM_BEGIN_PRIVATE, PEM_END_PRIVATE);
    normalized = stripPemWrapper(normalized, PEM_BEGIN_PUBLIC, PEM_END_PUBLIC);
    normalized = stripPemWrapper(normalized, PEM_BEGIN_RSA_PUBLIC, PEM_END_RSA_PUBLIC);
    return normalized.replace("\n", "").replace("\r", "");
  }

  private String stripPemWrapper(String pem, String begin, String end) {
    if (pem.contains(begin) && pem.contains(end)) {
      int start = pem.indexOf(begin) + begin.length();
      int endIdx = pem.indexOf(end);
      return pem.substring(start, endIdx);
    }
    return pem;
  }
}
