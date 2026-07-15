/* (C) 2026 */

package aros.services.rms.infraestructure.common.config;

import aros.services.rms.core.auth.domain.jwk.JwkKeyId;
import aros.services.rms.infraestructure.auth.jwk.JwkKeyIdDeriver;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.stereotype.Component;

/** Validator for JWT configuration. */
@Component
public class JwtConfigValidator {

  private static final Logger log = LoggerFactory.getLogger(JwtConfigValidator.class);
  private static final String PRODUCTION = "production";
  private static final String ERROR_MESSAGE =
      "JWT keys not configured. Run './gradlew generate-jwt-keys' or 'task jwtkeys' "
          + "to generate and add to .env file";
  private static final String PRODUCTION_ERROR_MESSAGE =
      "CRITICAL: JWT keys are required in production. "
          + "Application cannot start without JWT configuration.";
  private static final String MULTI_KEY_PREFIX = "JWT_PUBLIC_KEY_K_";

  private final String publicKey;
  private final String privateKey;
  private final String appEnv;
  private final Environment environment;
  private List<JwkKeyMaterial> cachedKeys;
  private JwkKeyId cachedActiveKid;

  /** Key material record holding kid, public key, and optional private key. */
  public record JwkKeyMaterial(JwkKeyId kid, RSAPublicKey publicKey, RSAPrivateKey privateKey) {}

  /**
   * Creates a new JwtConfigValidator.
   *
   * @param publicKey the JWT public key
   * @param privateKey the JWT private key
   * @param appEnv the application environment
   * @param environment the Spring environment for multi-key detection
   */
  public JwtConfigValidator(
      @Value("${app.jwt.public-key:}") String publicKey,
      @Value("${app.jwt.private-key:}") String privateKey,
      @Value("${app.env:development}") String appEnv,
      Environment environment) {
    this.publicKey = normalizeKey(publicKey);
    this.privateKey = normalizeKey(privateKey);
    this.appEnv = appEnv;
    this.environment = environment;
  }

  /** Validates the JWT configuration. */
  @PostConstruct
  public void validate() {
    boolean isProduction = PRODUCTION.equals(appEnv);
    boolean configured = isConfigured();

    if (!configured) {
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

  /**
   * Returns all configured JWT keys as key material records.
   *
   * @return list of JwkKeyMaterial, one per configured key
   */
  public List<JwkKeyMaterial> allKeys() {
    if (cachedKeys != null) {
      return cachedKeys;
    }
    if (!isConfigured()) {
      cachedKeys = List.of();
      return cachedKeys;
    }
    if (isMultiKey()) {
      cachedKeys = loadMultiKeys();
    } else {
      cachedKeys = loadSingleKey();
    }
    return cachedKeys;
  }

  /**
   * Returns the active signing key ID.
   *
   * @return the active JwkKeyId, or null if not configured
   */
  public JwkKeyId activeKeyId() {
    if (cachedActiveKid != null) {
      return cachedActiveKid;
    }
    if (!isConfigured()) {
      return null;
    }
    if (isMultiKey()) {
      String activeKid = environment.getProperty("JWT_ACTIVE_KID");
      if (activeKid == null || activeKid.isBlank()) {
        log.warn("JWT_ACTIVE_KID not set in multi-key mode, falling back to first key");
        List<JwkKeyMaterial> keys = allKeys();
        if (!keys.isEmpty()) {
          cachedActiveKid = keys.get(0).kid();
          return cachedActiveKid;
        }
        return null;
      }
      cachedActiveKid = new JwkKeyId("k-" + activeKid);
    } else {
      List<JwkKeyMaterial> keys = allKeys();
      cachedActiveKid = keys.isEmpty() ? null : keys.get(0).kid();
    }
    return cachedActiveKid;
  }

  private boolean isMultiKey() {
    return System.getenv().keySet().stream().anyMatch(k -> k.startsWith(MULTI_KEY_PREFIX));
  }

  private List<JwkKeyMaterial> loadSingleKey() {
    try {
      RSAPublicKey pub = parsePublicKey(publicKey);
      RSAPrivateKey priv = parsePrivateKey(privateKey);
      JwkKeyId kid = JwkKeyIdDeriver.from(pub);
      return List.of(new JwkKeyMaterial(kid, pub, priv));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse single JWT key", e);
    }
  }

  private List<JwkKeyMaterial> loadMultiKeys() {
    List<JwkKeyMaterial> keys = new ArrayList<>();
    for (String envName : System.getenv().keySet()) {
      if (envName.startsWith(MULTI_KEY_PREFIX)) {
        String kidValue = envName.substring(MULTI_KEY_PREFIX.length());
        String pubPem = System.getenv(envName);
        String privPem = System.getenv("JWT_PRIVATE_KEY_K_" + kidValue);
        if (pubPem == null || pubPem.isBlank()) {
          continue;
        }
        try {
          RSAPublicKey pub = parsePublicKey(pubPem);
          RSAPrivateKey priv =
              (privPem != null && !privPem.isBlank()) ? parsePrivateKey(privPem) : null;
          keys.add(new JwkKeyMaterial(new JwkKeyId("k-" + kidValue), pub, priv));
        } catch (Exception e) {
          log.warn("Failed to parse multi-key for kid={}: {}", kidValue, e.getMessage());
        }
      }
    }
    return List.copyOf(keys);
  }

  private RSAPublicKey parsePublicKey(String pem) {
    String normalized = normalizeKey(pem);
    return RsaKeyConverters.x509()
        .convert(new ByteArrayInputStream(normalized.getBytes(StandardCharsets.UTF_8)));
  }

  private RSAPrivateKey parsePrivateKey(String pem) {
    String normalized = normalizeKey(pem);
    return RsaKeyConverters.pkcs8()
        .convert(new ByteArrayInputStream(normalized.getBytes(StandardCharsets.UTF_8)));
  }

  /** Normalizes the key by replacing escaped newlines. */
  private String normalizeKey(String key) {
    if (key == null || key.isBlank()) {
      return key;
    }
    key = key.trim();
    key = key.replace("\\n", "\n");
    key = key.replace("\r", "");
    return key;
  }
}
