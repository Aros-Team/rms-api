/* (C) 2026 */
package aros.services.rms.infraestructure.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfigValidator {

  private static final Logger log = LoggerFactory.getLogger(JwtConfigValidator.class);
  private static final String ERROR_MESSAGE =
      "JWT keys not configured. Run './gradlew generate-jwt-keys' or 'task jwtkeys' to generate and add to .env file";

  private final String publicKey;
  private final String privateKey;
  private boolean validated = false;

  public JwtConfigValidator(
      @Value("${app.jwt.public-key:}") String publicKey,
      @Value("${app.jwt.private-key:}") String privateKey) {
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }

  public void validate() {
    if (!validated
        && (publicKey == null
            || publicKey.isBlank()
            || privateKey == null
            || privateKey.isBlank())) {
      log.error(ERROR_MESSAGE);
      validated = true;
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
}
