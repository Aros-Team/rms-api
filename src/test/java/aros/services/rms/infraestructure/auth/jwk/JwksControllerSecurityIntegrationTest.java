/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "app.jwt.public-key=unused",
      "app.jwt.private-key=unused",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    })
@AutoConfigureMockMvc
class JwksControllerSecurityIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @DynamicPropertySource
  static void configureJwtKeys(DynamicPropertyRegistry registry) throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();
    String pubPem =
        "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getEncoder().encodeToString(kp.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----";
    String privPem =
        "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded())
            + "\n-----END PRIVATE KEY-----";
    registry.add("app.jwt.public-key", () -> pubPem);
    registry.add("app.jwt.private-key", () -> privPem);
  }

  @Test
  void shouldAllowPublicAccessToJwksEndpoint() throws Exception {
    mockMvc
        .perform(get("/.well-known/jwks.json").accept("application/jwk-set+json"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldRejectAccessToProtectedEndpoint() throws Exception {
    mockMvc.perform(get("/api/auth")).andExpect(status().isUnauthorized());
  }
}
