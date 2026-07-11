/* (C) 2026 */

package aros.services.rms.testsupport;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base class for integration tests that need a valid RSA key pair for JWT signing and verifying.
 *
 * <p>Generates a fresh RSA key pair at context initialization and exposes it as the {@code
 * app.jwt.public-key} and {@code app.jwt.private-key} properties so {@code JwtEncoder} and {@code
 * JwtDecoder} beans can be created.
 */
public abstract class AbstractJwtIntegrationTest {

  @DynamicPropertySource
  static void registerJwtKeys(DynamicPropertyRegistry registry) {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048);
      KeyPair keyPair = keyGen.generateKeyPair();

      String publicKeyPem =
          "-----BEGIN PUBLIC KEY-----\n"
              + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
              + "\n-----END PUBLIC KEY-----";
      String privateKeyPem =
          "-----BEGIN PRIVATE KEY-----\n"
              + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
              + "\n-----END PRIVATE KEY-----";

      registry.add("app.jwt.public-key", () -> publicKeyPem);
      registry.add("app.jwt.private-key", () -> privateKeyPem);
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate JWT keys for tests", e);
    }
  }
}
