/* (C) 2026 */

package aros.services.rms.testsupport;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Tests for {@link for}.
 *
 * <p><b>Feature:</b> JWT integration test base class
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>register Jwt Keys
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Verifies business logic with unit-level assertions
 * </ul>
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
